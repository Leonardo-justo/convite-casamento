'use client';

import { useState } from 'react';

export default function Home() {
  const [isOpen, setIsOpen] = useState(false);

  return (
    <main className={`invitation-shell${isOpen ? ' is-open' : ''}`}>
      <section
        className="invitation-stage"
        aria-label="Convite de casamento de Bruna e Leonardo"
      >
        <button
          type="button"
          className="invitation-page cover-page"
          onClick={() => setIsOpen(true)}
          aria-label="Abrir o convite de casamento"
          tabIndex={isOpen ? -1 : 0}
        >
          <img
            src="/convite/capa.png"
            alt="Capa do convite de casamento de Bruna e Leonardo"
            draggable={false}
          />
        </button>

        <div className="invitation-page opened-page" aria-hidden={!isOpen}>
          <img
            src="/convite/convite.png"
            alt="Convite de casamento de Bruna e Leonardo para 7 de novembro de 2026, às 19 horas"
            draggable={false}
          />
        </div>
      </section>
    </main>
  );
}
