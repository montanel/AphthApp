/*
 * support.js: a small stand-in runtime so the AphthApp design files (*.dc.html) open in any browser,
 * without the Claude design canvas. Written for this handoff; it covers what these files use:
 *   {{ path }} holes in text and attributes, <sc-for list as>, <sc-if value>, <helmet>,
 *   onClick / onDoubleClick / onChange handlers, and a `class Component extends DCLogic`
 *   with constructor state, setState and renderVals().
 * It is only for looking at the designs. The app itself is built natively.
 */
(function () {
  class DCLogic {
    constructor(props) { this.props = props || {}; this.state = {}; }
    setState(patch) { Object.assign(this.state, typeof patch === 'function' ? patch(this.state) : patch); if (this.__render) this.__render(); }
    forceUpdate() { if (this.__render) this.__render(); }
  }
  window.DCLogic = DCLogic;

  const esc = (s) => String(s).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
  const HOLE = /\{\{\s*([^}]+?)\s*\}\}/g;
  const ONLY = /^\s*\{\{\s*([^}]+?)\s*\}\}\s*$/;
  const EVENTS = { onclick: 'click', ondoubleclick: 'dblclick', onchange: 'change', oninput: 'input' };
  const VOID = new Set(['area', 'base', 'br', 'col', 'embed', 'hr', 'img', 'input', 'link', 'meta', 'source', 'track', 'wbr']);

  function lookup(path, scopes) {
    path = path.trim();
    if (path === 'true') return true;
    if (path === 'false') return false;
    if (/^-?\d+(\.\d+)?$/.test(path)) return Number(path);
    if (/^(['"]).*\1$/.test(path)) return path.slice(1, -1);
    const parts = path.split('.');
    for (const scope of scopes) {
      if (scope && Object.prototype.hasOwnProperty.call(scope, parts[0])) {
        let v = scope[parts[0]];
        for (let i = 1; i < parts.length && v != null; i++) v = v[parts[i]];
        return v;
      }
    }
    return undefined;
  }
  const text = (s, scopes) => s.replace(HOLE, (_, p) => { const v = lookup(p, scopes); return v == null ? '' : String(v); });

  function render(node, scopes, handlers) {
    const kids = (n, sc) => Array.from((n.content || n).childNodes).map((c) => render(c, sc, handlers)).join('');
    if (node.nodeType === 3) return esc(text(node.nodeValue, scopes));
    if (node.nodeType === 11) return kids(node, scopes);
    if (node.nodeType !== 1) return '';
    const tag = node.localName;
    if (tag === 'helmet') return '';
    if (tag === 'sc-if') return lookup(ONLY.exec(node.getAttribute('value') || '')?.[1] || 'false', scopes) ? kids(node, scopes) : '';
    if (tag === 'sc-for') {
      const list = lookup(ONLY.exec(node.getAttribute('list') || '')?.[1] || '', scopes) || [];
      const as = node.getAttribute('as') || 'item';
      return list.map((item, i) => kids(node, [{ [as]: item, $index: i }, ...scopes])).join('');
    }
    let attrs = '';
    for (const a of Array.from(node.attributes)) {
      const name = a.name;
      if (name.startsWith('hint-')) continue;
      const only = ONLY.exec(a.value);
      if (EVENTS[name]) {
        const fn = only ? lookup(only[1], scopes) : null;
        if (typeof fn === 'function') { handlers.push([EVENTS[name], fn]); attrs += ` data-dc-ev${handlers.length - 1}="${EVENTS[name]}"`; }
        continue;
      }
      const v = only ? lookup(only[1], scopes) : text(a.value, scopes);
      if (v === false || v == null) { if (name.startsWith('aria-')) attrs += ` ${name}="false"`; continue; }
      attrs += ` ${name}="${esc(v === true ? (name.startsWith('aria-') ? 'true' : '') : v)}"`;
    }
    if (VOID.has(tag)) return `<${tag}${attrs}>`;
    return `<${tag}${attrs}>${kids(node, scopes)}</${tag}>`;
  }

  function boot() {
    const host = document.querySelector('x-dc');
    if (!host) return;
    const helmet = host.querySelector('helmet');
    if (helmet) Array.from(helmet.children).forEach((el) => {
      const copy = document.createElement(el.localName);
      Array.from(el.attributes).forEach((a) => copy.setAttribute(a.name, a.value));
      copy.textContent = el.textContent;
      document.head.appendChild(copy);
    });
    const tpl = document.createElement('template');
    tpl.innerHTML = host.innerHTML;
    const script = document.querySelector('script[data-dc-script]');
    let props = {};
    try {
      const decl = JSON.parse(script.getAttribute('data-props') || '{}');
      for (const k of Object.keys(decl)) if (!k.startsWith('$') && 'default' in decl[k]) props[k] = decl[k].default;
    } catch (e) { /* no props */ }
    const Component = new Function('DCLogic', script.textContent + '\nreturn Component;')(DCLogic);
    const inst = new Component(props);
    inst.props = Object.assign({}, props, inst.props);
    const draw = () => {
      const handlers = [];
      host.innerHTML = render(tpl.content, [inst.renderVals() || {}], handlers);
      handlers.forEach(([ev, fn], i) => {
        const el = host.querySelector(`[data-dc-ev${i}]`);
        if (el) el.addEventListener(ev, fn);
      });
    };
    inst.__render = draw;
    draw();
    if (inst.componentDidMount) inst.componentDidMount();
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', boot); else boot();
})();
