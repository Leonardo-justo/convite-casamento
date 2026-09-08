import { readFile, writeFile, cp, mkdir, mkdtemp, rm } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { build } from 'vite';
import react from '@vitejs/plugin-react';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';

const root = fileURLToPath(new URL('../', import.meta.url));
const output = path.join(root, 'dist-pages');
const temporary = await mkdtemp(path.join(root, '.pages-build-'));
try {
  // Bundle shared UI components, then render the same page used locally.
  await build({ configFile: false, root, plugins: [react()],
    resolve: { alias: { '@': root } },
    build: { ssr: path.join(root, 'app/page.tsx'), outDir: temporary, emptyOutDir: false,
      rollupOptions: { output: { entryFileNames: 'page.mjs' } } },
  });
  const modulePath = path.join(temporary, 'page.mjs');
  const { default: Home } = await import(pathToFileURL(modulePath).href);
  const markup = renderToStaticMarkup(React.createElement(Home));
  const css = (await readFile(path.join(root, 'app/globals.css'), 'utf8')).replace(/@import\s+['"]tailwindcss['"];?/, '');
  if (path.dirname(output) !== path.resolve(root) || path.basename(output) !== 'dist-pages') {
    throw new Error('Invalid output directory');
  }
  await rm(output, { recursive: true, force: true });
  await mkdir(output, { recursive: true });
  await cp(path.join(root, 'public/convite-atual'), path.join(output, 'convite-atual'), { recursive: true });
  await cp(path.join(root, 'public/invitation-navigation.js'), path.join(output, 'invitation-navigation.js'));
  await writeFile(path.join(output, 'style.css'), css);
  await writeFile(path.join(output, '.nojekyll'), '');
  await writeFile(path.join(output, 'index.html'), `<!doctype html>
<html lang="pt-BR"><head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Bruna e Leonardo | Convite de casamento</title>
<meta name="description" content="Convite de casamento de Bruna e Leonardo.">
<meta name="robots" content="noindex, nofollow">
<meta property="og:title" content="Bruna e Leonardo | Convite de casamento">
<meta property="og:description" content="Convite de casamento de Bruna e Leonardo.">
<meta property="og:type" content="website">
<meta property="og:image" content="./convite-atual/pagina-1.png">
<meta name="twitter:card" content="summary_large_image">
<link rel="stylesheet" href="./style.css">
</head><body>${markup}</body></html>`);
  console.log('GitHub Pages pronto: dist-pages/index.html');
} finally {
  await rm(temporary, { recursive: true, force: true });
}
