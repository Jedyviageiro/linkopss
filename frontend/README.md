# LinkOps Frontend

Base Vue 3 + TypeScript preparada para consumir a API Spring Boot do LinkOps. O código está organizado por funcionalidade para permitir que diferentes pessoas trabalhem em módulos sem criar dependências desnecessárias.

## Executar localmente

Requisitos: Node.js `22.22.2+` (ou `24.15+`) e a API disponível em `http://localhost:8081`.

```powershell
Copy-Item .env.example .env.local
npm install
npm run dev
```

O frontend abre normalmente em `http://localhost:5173`. Para validar tipos e gerar o bundle:

```powershell
npm run build
```

## Variáveis de ambiente

```dotenv
VITE_API_BASE_URL=http://localhost:8081/api/v1
```

Nunca colocar segredos no frontend. Variáveis com prefixo `VITE_` ficam visíveis no bundle entregue ao navegador.

## Estrutura

```text
src/
├── app/                 # Router, layouts e páginas globais
├── assets/              # CSS e recursos visuais
├── config/              # Leitura/validação do ambiente
├── features/            # Módulos de negócio
│   ├── auth/
│   ├── users/
│   ├── providers/
│   ├── categories/
│   ├── services/
│   ├── bookings/
│   ├── payment/
│   ├── reviews/
│   ├── location/
│   ├── media/
│   ├── notifications/
│   └── admin/
└── shared/              # Cliente HTTP, tokens, componentes e tipos comuns
```

Cada feature deve manter os seus `api/`, `types/`, `stores/`, `components/` e `views/` dentro do próprio diretório. Só mover algo para `shared/` quando for realmente usado por vários módulos.

## Integração com a API

As funções de cada endpoint ficam em `features/<módulo>/api`. O cliente comum acrescenta o Bearer token, converte JSON, normaliza os erros da API e tenta renovar a sessão uma vez quando recebe `401`.

```ts
import { servicesApi } from '@/features/services/api/services-api'

const result = await servicesApi.list({
  q: 'canalizador',
  city: 'Maputo',
  page: 0,
  size: 20,
})

console.log(result.content, result.page.totalElements)
```

Login, registo, recuperação da palavra-passe, rotas protegidas e controlo de acesso por role já estão ligados ao backend. `/services` é uma página funcional de referência com filtros e paginação; o design pode ser substituído sem alterar a camada de API.

## Tailwind CSS

O projeto usa Tailwind CSS 4 através do plugin oficial do Vite. Não é necessário criar `tailwind.config.js` ou `postcss.config.js` para a configuração atual. Os tokens iniciais da marca estão em `src/assets/styles/main.css` e podem ser usados como `text-linkops-600`, `bg-linkops-800`, etc.

Para adicionar ou alterar tokens, use o bloco `@theme`. A equipa deve preferir utilities Tailwind em novas páginas e extrair componentes Vue quando um padrão visual/comportamental se repetir.

## Backlog da equipa

O plano completo, com tarefas, papéis, validações e as 50 operações da API, está em [FRONTEND_IMPLEMENTATION_PLAN.md](./FRONTEND_IMPLEMENTATION_PLAN.md).

## Autenticação

O estado da sessão está em `features/auth/stores/auth-store.ts`. As rotas usam `requiresAuth`, `guestOnly` e `roles` nos metadados. Atualmente os tokens são guardados em `localStorage` porque a API devolve ambos no corpo da resposta. Antes de produção, o refresh token deve preferencialmente migrar para um cookie `HttpOnly`, `Secure` e `SameSite` emitido pelo backend.
