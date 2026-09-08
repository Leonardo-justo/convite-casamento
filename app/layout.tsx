import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  metadataBase: new URL('https://leonardo-justo.github.io/convite-casamento/'),
  title: 'Bruna e Leonardo | Convite de casamento',
  description:
    'Com carinho, convidamos voce para celebrar o casamento de Bruna e Leonardo.',
  openGraph: {
    title: 'Bruna e Leonardo | Convite de casamento',
    description:
      'Com carinho, convidamos voce para celebrar o casamento de Bruna e Leonardo.',
    url: 'https://leonardo-justo.github.io/convite-casamento/',
    siteName: 'Convite de casamento de Bruna e Leonardo',
    locale: 'pt_BR',
    type: 'website',
    images: [{ url: '/convite-atual/pagina-1.png', width: 2400, height: 2400 }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Bruna e Leonardo | Convite de casamento',
    description:
      'Com carinho, convidamos voce para celebrar o casamento de Bruna e Leonardo.',
    images: ['/convite-atual/pagina-1.png'],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
