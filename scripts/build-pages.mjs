import { readFile, writeFile, copyFile, cp, mkdir, mkdtemp, rm } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import ts from 'typescript';
import React from 'react';
import { renderToStaticMarkup } from 'react-dom/server';

const root = fileURLToPath(new URL('../', import.meta.url));
const output = path.join(root, 'dist-pages');
const temporary = await mkdtemp(path.join(root, '.pages-build-'));
try {
  // Render the same React page used locally into static, JavaScript-free HTML.
  const source = await readFile(path.join(root, 'app/page.tsx'), 'utf8');
  const compiled = ts.transpileModule(source, {
    compilerOptions: { jsx: ts.JsxEmit.ReactJSX, module: ts.ModuleKind.ESNext, target: ts.ScriptTarget.ES2022 },
  });
  const modulePath = path.join(temporary, 'page.mjs');
  await writeFile(modulePath, compiled.outputText);
  const { default: Home } = await import(pathToFileURL(modulePath).href);
  const markup = renderToStaticMarkup(React.createElement(Home));
  const css = (await readFile(path.join(root, 'app/globals.css'), 'utf8')).replace(/@import\s+['"]tailwindcss['"];?/, '');
  await mkdir(output, { recursive: true });
  await cp(path.join(root, 'public/convite-novo'), path.join(output, 'convite-novo'), { recursive: true });
  await copyFile(path.join(root, 'public/og.png'), path.join(output, 'og.png'));
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
<meta property="og:image" content="./og.png">
<meta name="twitter:card" content="summary_large_image">
<link rel="stylesheet" href="./style.css">
</head><body>${markup}</body></html>`);
  console.log('GitHub Pages pronto: dist-pages/index.html');
} finally {
  await rm(temporary, { recursive: true, force: true });
}
