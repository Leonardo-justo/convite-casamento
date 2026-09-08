import type { CSSProperties } from 'react';

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
  {
    title: 'Confirme sua Presença',
    description: 'Sua presença tornará nosso dia ainda mais especial! Para que possamos preparar cada detalhe com carinho, pedimos a gentileza de confirmar sua presença até o dia 7 de outubro de 2026, através do nosso site. A confirmação é obrigatória para a sua entrada no evento. Esperamos vocês lá!',
    links: [{ label: 'Confirmar presença no site dos noivos (abre em nova aba)', rect: [163.281525, 187.77249, 404.06161, 231.27795], href: 'https://noivos.casar.com/leonardo-s2-bruna#/rsvp', external: true }],
  },
];

// Convert bottom-left PDF coordinates, including the page's nonzero origin.
function hotspotStyle(rect: number[]): CSSProperties {
  const [x0, y0, x1, y1] = rect;
  const left = Math.max(0, x0), right = Math.min(567, x1);
  const bottom = Math.max(8.039995, y0), top = Math.min(575.04, y1);
  return { left: `${left / 567 * 100}%`, top: `${(575.04 - top) / 567 * 100}%`, width: `${(right - left) / 567 * 100}%`, height: `${(top - bottom) / 567 * 100}%` };
}

export default function Home() {
  return (
    <main className="invitation-viewer">
      <h1 className="sr-only">Convite de casamento de Bruna e Leonardo</h1>
      <div className="invitation-shell">
        {pages.map((page, index) => (
          <section key={page.title} id={`pagina-${index + 1}`} className="pdf-page"
            aria-label={page.title}>
            <img src={`./convite-atual/pagina-${index + 1}.png`} width={2400} height={2400}
              alt={page.description} draggable={false} decoding="async"
              loading={index === 0 ? 'eager' : 'lazy'} fetchPriority={index === 0 ? 'high' : 'auto'} />
            {page.links.map((link) => (
              <a key={link.label} className="pdf-hotspot" href={link.href} style={hotspotStyle(link.rect)}
                aria-label={link.label} title={link.label}
                target={link.external ? '_blank' : undefined}
                rel={link.external ? 'noopener noreferrer' : undefined} />
            ))}
          </section>
        ))}
      </div>
    </main>
  );
}
