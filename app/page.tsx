'use client';

import { useEffect, useRef, useState, type CSSProperties } from 'react';
import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Pagination, PaginationContent, PaginationItem, PaginationLink } from '@/components/ui/pagination';

const siteUrl = 'https://noivos.casar.com/leonardo-s2-bruna';
type Hotspot = { label: string; rect: number[]; href: string; external?: boolean };
const pages: { title: string; description: string; links: Hotspot[] }[] = [
  {
    title: 'Capa',
    description: 'Envelope floral com o monograma B | L e a indicação clique aqui para abrir o convite.',
    links: [{ label: 'Abrir o convite', rect: [223.69646, 154.744568, 342.96136, 280], href: '#pagina-2' }],
  },
  {
    title: 'Convite',
    description: 'Bruna e Leonardo. Com a benção de Deus e de seus pais, convidam para a celebração de seu Matrimônio. 07 de novembro de 2026, às 19 horas. Paróquia São Luís Gonzaga, Rua São Luiz, nº 250, Centro, Cedral – São Paulo. Após a cerimônia, os noivos recepcionarão os convidados na Chácara Viva Águas Claras I, Rodovia Washington Luís, km 427, São José do Rio Preto – SP. Mais informações e confirmação de presença através do site dos noivos.',
    links: [
      { label: 'Avançar para os locais', rect: [472.68369, -14.2561035, 591.94861, 74.254974], href: '#pagina-3' },
      { label: 'Informações e confirmação de presença no site dos noivos (abre em nova aba)', rect: [164, 67, 405, 84], href: siteUrl, external: true },
    ],
  },
  {
    title: 'Local',
    description: 'Cerimônia na Paróquia São Luís Gonzaga, Rua São Luiz, nº 250, Centro, Cedral – São Paulo. Recepção na Chácara Viva Águas Claras I. Rod. Washington Luís, 426, Cedral - SP, 15895-000.',
    links: [
      { label: 'Ver localização da cerimônia no Google Maps (abre em nova aba)', rect: [314.60199, 222.95563, 527.6286, 267.96124], href: 'https://maps.app.goo.gl/WsmCB87bk2KB2YqDA', external: true },
      { label: 'Ver localização da recepção no Google Maps (abre em nova aba)', rect: [177.17215, 30.873108, 390.19876, 75.878723], href: 'https://maps.app.goo.gl/ufVpJmGGVeGAmkb47', external: true },
      { label: 'Avançar para as informações gerais', rect: [472.68369, -14.2561035, 591.94861, 74.254974], href: '#pagina-4' },
    ],
  },
  {
    title: 'Informações gerais',
    description: 'Traje: sugerimos aos nossos convidados o uso de traje social. Pedimos, gentilmente, que evitem bermudas e camisetas. Horário: a cerimônia terá início pontualmente as 19H. Recomendamos a chegada com 30 minutos de antecedência. Site dos noivos: preparamos um espaço especial com nossa lista de presentes e outras informações sobre o casamento.',
    links: [{ label: 'Acessar o site dos noivos (abre em nova aba)', rect: [295.19876, 30.102905, 527.72784, 75.858612], href: siteUrl, external: true }],
  },
  { title: 'Página final', description: 'Última página do PDF original, com fundo creme e sem texto.', links: [] },
];

// Convert bottom-left PDF coordinates, including the page's nonzero origin.
function hotspotStyle(rect: number[]): CSSProperties {
  const [x0, y0, x1, y1] = rect;
  const left = Math.max(0, x0), right = Math.min(567, x1);
  const bottom = Math.max(8.039995, y0), top = Math.min(575.04, y1);
  return { left: `${left / 567 * 100}%`, top: `${(575.04 - top) / 567 * 100}%`, width: `${(right - left) / 567 * 100}%`, height: `${(top - bottom) / 567 * 100}%` };
}

function pageFromHash() {
  const match = /^#pagina-([1-5])$/.exec(window.location.hash);
  return match ? Number(match[1]) - 1 : 0;
}

export default function Home() {
  const [current, setCurrent] = useState(0);
  const [previous, setPrevious] = useState<number | null>(null);
  const [direction, setDirection] = useState('forward');
  const currentRef = useRef(0);
  const stageRef = useRef<HTMLDivElement>(null);
  const touchRef = useRef<{ x: number; y: number } | null>(null);

  useEffect(() => {
    currentRef.current = pageFromHash();
    setCurrent(currentRef.current);
    const onHashChange = () => {
      const next = pageFromHash();
      if (next === currentRef.current) return;
      setPrevious(currentRef.current);
      setDirection(next > currentRef.current ? 'forward' : 'backward');
      currentRef.current = next;
      setCurrent(next);
      stageRef.current?.focus({ preventScroll: true });
    };
    window.addEventListener('hashchange', onHashChange);
    return () => window.removeEventListener('hashchange', onHashChange);
  }, []);

  function goTo(next: number) {
    if (next >= 0 && next < pages.length) window.location.hash = `pagina-${next + 1}`;
  }

  return (
    <main className="invitation-viewer" onKeyDown={(event) => {
      if (event.altKey || event.ctrlKey || event.metaKey) return;
      if (event.key === 'ArrowRight') { event.preventDefault(); goTo(current + 1); }
      if (event.key === 'ArrowLeft') { event.preventDefault(); goTo(current - 1); }
    }}>
      <h1 className="sr-only">Convite de casamento de Bruna e Leonardo</h1>
      <div className="invitation-shell">
        <div className={`invitation-stage ${direction}`} ref={stageRef} tabIndex={-1}
          role="region" aria-label={`Página ${current + 1} de 5: ${pages[current].title}`}
          onTouchStart={(event) => {
            const target = event.target as HTMLElement;
            touchRef.current = event.touches.length === 1 && !target.closest('a, button')
              ? { x: event.touches[0].clientX, y: event.touches[0].clientY } : null;
          }}
          onTouchMove={(event) => { if (event.touches.length !== 1) touchRef.current = null; }}
          onTouchCancel={() => { touchRef.current = null; }}
          onTouchEnd={(event) => {
            const start = touchRef.current;
            touchRef.current = null;
            if (!start || !event.changedTouches.length || (window.visualViewport?.scale ?? 1) > 1.05) return;
            const dx = event.changedTouches[0].clientX - start.x;
            const dy = event.changedTouches[0].clientY - start.y;
            if (Math.abs(dx) > 65 && Math.abs(dx) > Math.abs(dy) * 1.5) goTo(current + (dx < 0 ? 1 : -1));
          }}>
          {pages.map((page, index) => (
            <section key={index} className={`pdf-page ${index === current ? 'active' : index === previous ? 'leaving' : 'parked'}${previous !== null && index === current ? 'entering' : ''}`}
              inert={index !== current} aria-hidden={index !== current}
              onAnimationEnd={(event) => { if (event.target === event.currentTarget && index === current) setPrevious(null); }}>
              <img src={`/convite-novo/pagina-${index + 1}.png`} width={2400} height={2400}
                alt={page.description} draggable={false} decoding="async"
                loading={index < 2 ? 'eager' : 'lazy'} fetchPriority={index === 0 ? 'high' : 'auto'} />
              {page.links.map((link) => (
                <a key={link.label} className="pdf-hotspot" href={link.href} style={hotspotStyle(link.rect)}
                  aria-label={link.label} title={link.label}
                  target={link.external ? '_blank' : undefined}
                  rel={link.external ? 'noopener noreferrer' : undefined} />
              ))}
            </section>
          ))}
        </div>
        <Pagination className="page-navigation" aria-label="Páginas do convite">
          <button className="page-arrow" type="button" disabled={current === 0} onClick={() => goTo(current - 1)} aria-label="Página anterior"><ChevronLeft aria-hidden="true" /></button>
          <PaginationContent className="page-numbers">
            {pages.map((page, index) => (
              <PaginationItem key={page.title}>
                <PaginationLink className="page-number" href={`#pagina-${index + 1}`} isActive={current === index}
                  aria-label={`Página ${index + 1}: ${page.title}`}>{index + 1}</PaginationLink>
              </PaginationItem>
            ))}
          </PaginationContent>
          <button className="page-arrow" type="button" disabled={current === pages.length - 1} onClick={() => goTo(current + 1)} aria-label="Próxima página"><ChevronRight aria-hidden="true" /></button>
        </Pagination>
        <p className="page-caption" aria-live="polite" aria-atomic="true">{current + 1} / 5 · {pages[current].title}</p>
      </div>
    </main>
  );
}
