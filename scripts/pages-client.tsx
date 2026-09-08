import React from 'react';
import { hydrateRoot } from 'react-dom/client';
import Home from '../app/page';

const container = document.getElementById('invitation-root');
if (container) hydrateRoot(container, <Home />);
