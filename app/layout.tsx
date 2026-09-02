import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'Bruna e Leonardo | Convite de casamento',
  description: 'Convite de casamento de Bruna e Leonardo.',
  openGraph: {
    title: 'Bruna e Leonardo | Convite de casamento',
    description: 'Convite de casamento de Bruna e Leonardo.',
    type: 'website',
    images: [{ url: '/og.png', width: 3494, height: 2481 }],
  },
  twitter: {
    card: 'summary_large_image',
    title: 'Bruna e Leonardo | Convite de casamento',
    description: 'Convite de casamento de Bruna e Leonardo.',
    images: ['/og.png'],
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
