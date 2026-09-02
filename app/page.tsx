'use client';

import { useMemo, useState } from 'react';
import {
  CalendarHeart,
  ChevronDown,
  Gift,
  Heart,
  MapPin,
  MessageCircleHeart,
  Send,
} from 'lucide-react';

const weddingDate = new Date('2026-11-07T19:00:00-03:00');

const ceremonyMapUrl =
  'https://www.google.com/maps/search/?api=1&query=Pra%C3%A7a%20S%C3%A3o%20Luiz%2C%20247%2C%20Centro%2C%20Cedral%20-%20SP';

const receptionMapUrl =
  'https://www.google.com/maps/search/?api=1&query=Ch%C3%A1cara%20%C3%81guas%20Claras%20Viva%201%2C%20S%C3%A3o%20Jos%C3%A9%20do%20Rio%20Preto%20-%20SP';

const casarBaseUrl = 'https://noivos.casar.com/leonardo-s2-bruna';

function formatTimeLeft() {
  const now = new Date();
  const diff = weddingDate.getTime() - now.getTime();

  if (diff <= 0) {
    return { days: '00', hours: '00', minutes: '00' };
  }

  const totalMinutes = Math.floor(diff / 60000);
  const days = Math.floor(totalMinutes / 1440);
  const hours = Math.floor((totalMinutes % 1440) / 60);
  const minutes = totalMinutes % 60;

  return {
    days: String(days).padStart(2, '0'),
    hours: String(hours).padStart(2, '0'),
    minutes: String(minutes).padStart(2, '0'),
  };
}

export default function Home() {
  const [isOpen, setIsOpen] = useState(false);
  const timeLeft = useMemo(formatTimeLeft, []);

  return (
    <main className={`wedding-site${isOpen ? ' is-open' : ''}`}>
      <section className="cover-experience" aria-label="Abertura do convite">
        <div
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
        </div>

        <a className="scroll-cue" href="#inicio" aria-label="Ir para o site">
          <ChevronDown aria-hidden="true" />
        </a>
      </section>

      <header className="site-header" id="inicio">
        <a className="brand-mark" href="#inicio" aria-label="Início">
          <span>B</span>
          <Heart aria-hidden="true" />
          <span>L</span>
        </a>
        <nav className="site-nav" aria-label="Menu principal">
          <a href="#casal">O casal</a>
          <a href="#cerimonia">Cerimônia</a>
          <a href="#recepcao">Recepção</a>
          <a href="#presentes">Presentes</a>
          <a href="#presenca">Presença</a>
          <a href="#recados">Recados</a>
        </nav>
      </header>

      <section className="hero-section">
        <div className="hero-copy">
          <p className="eyebrow">07 | 11 | 2026</p>
          <h1>Bruna & Leonardo</h1>
          <p>
            Estamos preparando esse dia com muito carinho para celebrar ao lado
            das pessoas que fazem parte da nossa história.
          </p>
          <div className="hero-actions" aria-label="Ações principais">
            <a className="primary-action" href="#presenca">
              <CalendarHeart aria-hidden="true" />
              Confirmar presença
            </a>
            <a className="secondary-action" href="#cerimonia">
              <MapPin aria-hidden="true" />
              Ver locais
            </a>
          </div>
        </div>

        <div className="countdown-card" aria-label="Contagem para o casamento">
          <span>Faltam</span>
          <div className="countdown-grid">
            <strong>{timeLeft.days}</strong>
            <strong>{timeLeft.hours}</strong>
            <strong>{timeLeft.minutes}</strong>
            <small>dias</small>
            <small>horas</small>
            <small>min</small>
          </div>
        </div>
      </section>

      <section className="content-section story-section" id="casal">
        <div className="section-kicker">O casal</div>
        <h2>Uma nova parte da nossa história começa aqui.</h2>
        <p>
          Histórias de amor existem e a nossa chegou nesse momento tão esperado:
          vamos nos casar. Queremos viver cada detalhe perto da família e dos
          amigos que tornam essa caminhada ainda mais especial.
        </p>
      </section>

      <section className="details-grid" aria-label="Detalhes do casamento">
        <article className="detail-panel" id="cerimonia">
          <span className="panel-icon">
            <CalendarHeart aria-hidden="true" />
          </span>
          <p className="section-kicker">Cerimônia</p>
          <h2>07 de novembro de 2026, às 19h</h2>
          <p>
            Praça São Luiz, 247, Centro, Cedral - SP. A celebração será
            conduzida com pontualidade para receber todos com tranquilidade.
          </p>
          <a className="link-button" href={ceremonyMapUrl} target="_blank">
            <MapPin aria-hidden="true" />
            Abrir no mapa
          </a>
        </article>

        <article className="detail-panel" id="recepcao">
          <span className="panel-icon">
            <Heart aria-hidden="true" />
          </span>
          <p className="section-kicker">Recepção</p>
          <h2>A partir das 20h30</h2>
          <p>
            Chácara Águas Claras - Viva 1, em São José do Rio Preto - SP.
            Depois da cerimônia, a festa continua com todos vocês.
          </p>
          <a className="link-button" href={receptionMapUrl} target="_blank">
            <MapPin aria-hidden="true" />
            Abrir no mapa
          </a>
        </article>
      </section>

      <section className="action-band" id="presentes">
        <div>
          <p className="section-kicker">Lista de presentes</p>
          <h2>Seu carinho faz parte desse momento.</h2>
          <p>
            Para quem desejar nos presentear, deixamos um caminho direto para a
            lista preparada com amor.
          </p>
        </div>
        <a className="primary-action" href={`${casarBaseUrl}#presentes`}>
          <Gift aria-hidden="true" />
          Ver lista
        </a>
      </section>

      <section className="split-section" id="presenca">
        <div>
          <p className="section-kicker">Confirme sua presença</p>
          <h2>Ajude a gente a preparar tudo com cuidado.</h2>
          <p>
            A confirmação oficial continua no nosso espaço do Casar.com, onde
            os dados ficam organizados para o grande dia.
          </p>
        </div>
        <a className="link-button large" href={`${casarBaseUrl}#rsvp`}>
          <CalendarHeart aria-hidden="true" />
          Confirmar agora
        </a>
      </section>

      <section className="message-section" id="recados">
        <div>
          <p className="section-kicker">Recados</p>
          <h2>Deixe uma mensagem de carinho.</h2>
          <p>
            Vamos amar ler cada recado. O envio também leva para o nosso espaço
            oficial, para ficar tudo reunido.
          </p>
        </div>
        <a className="secondary-action" href={`${casarBaseUrl}#recados`}>
          <MessageCircleHeart aria-hidden="true" />
          Escrever recado
        </a>
      </section>

      <footer className="site-footer">
        <span>Bruna</span>
        <Heart aria-hidden="true" />
        <span>Leonardo</span>
        <a href="#inicio" aria-label="Voltar ao início">
          <Send aria-hidden="true" />
        </a>
      </footer>
    </main>
  );
}
