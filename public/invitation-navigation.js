const viewer = document.querySelector('.invitation-viewer');
if (viewer && !viewer.dataset.navigationReady) {
  viewer.dataset.navigationReady = 'true';
  const sections = [...viewer.querySelectorAll('.invitation-section')];
  const links = [...viewer.querySelectorAll('[data-page-link]')];
  const previous = viewer.querySelector('[data-previous]');
  const next = viewer.querySelector('[data-next]');
  const status = viewer.querySelector('.navigation-status');
  let current = -1;
  let pending = false;

  function mark(index) {
    if (current === index) return;
    current = index;
    sections.forEach((section, i) => section.classList.toggle('is-current', i === index));
    links.forEach((link, i) => {
      if (i === index) link.setAttribute('aria-current', 'page');
      else link.removeAttribute('aria-current');
    });
    for (const [control, target] of [[previous, index - 1], [next, index + 1]]) {
      const disabled = target < 0 || target >= sections.length;
      control.setAttribute('aria-disabled', String(disabled));
      control.tabIndex = disabled ? -1 : 0;
      if (disabled) control.removeAttribute('href');
      else control.setAttribute('href', `#${sections[target].id}`);
    }
    status.textContent = `${index + 1} de ${sections.length}`;
  }

  function sync() {
    pending = false;
    const middle = window.innerHeight / 2;
    let nearest = 0, distance = Infinity;
    sections.forEach((section, i) => {
      const rect = section.getBoundingClientRect();
      const delta = Math.abs(rect.top + rect.height / 2 - middle);
      if (delta < distance) { nearest = i; distance = delta; }
    });
    mark(nearest);
  }

  function schedule() { if (!pending) { pending = true; requestAnimationFrame(sync); } }
  viewer.addEventListener('click', (event) => {
    const link = event.target.closest('a[href^="#pagina-"]');
    if (!link || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
    const target = sections.find(section => `#${section.id}` === link.getAttribute('href'));
    if (!target) return;
    event.preventDefault();
    if (location.hash !== `#${target.id}`) history.pushState(null, '', `#${target.id}`);
    target.scrollIntoView({ behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth', block: 'start' });
  });
  window.addEventListener('scroll', schedule, { passive: true });
  window.addEventListener('resize', schedule, { passive: true });
  window.addEventListener('hashchange', schedule);
  document.addEventListener('keydown', event => {
    if (event.altKey || event.ctrlKey || event.metaKey || event.shiftKey || event.target.closest('input, textarea, select, [contenteditable="true"]')) return;
    const offset = ['ArrowRight', 'PageDown'].includes(event.key) ? 1 : ['ArrowLeft', 'PageUp'].includes(event.key) ? -1 : 0;
    if (!offset) return;
    const target = current + offset;
    if (target >= 0 && target < links.length) { event.preventDefault(); links[target].click(); }
  });
  sync();
}
