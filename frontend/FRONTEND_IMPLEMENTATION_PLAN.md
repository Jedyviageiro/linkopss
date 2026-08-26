# Plano de implementação do frontend LinkOps

Este documento é o backlog recomendado para a equipa de frontend. A API base é `VITE_API_BASE_URL`, normalmente `http://localhost:8081/api/v1`. Os contratos TypeScript e as funções HTTP já existem em `src/features/*/api` e `src/features/*/types`; não devem ser duplicados dentro dos componentes.

## 1. Regras de trabalho

- Trabalhar por feature: página, componentes, store, tipos e testes ficam no módulo correspondente.
- Usar Composition API, `<script setup lang="ts">` e imports com `@/`.
- Usar Tailwind CSS para novo layout. Reutilizar tokens `linkops-*` definidos em `src/assets/styles/main.css`.
- Componentes não chamam `fetch` diretamente. Usar sempre os módulos `*Api` existentes.
- Tratar em todos os pedidos: carregamento, sucesso, vazio, erro de validação, `401`, `403`, `404` e erro de rede.
- Não mostrar ações que o papel atual não pode executar. A API continua a ser a autoridade final.
- Não enviar campos que a API não aceita e nunca guardar PIN, credenciais M-Pesa ou outros dados financeiros.
- Todos os textos dirigidos ao utilizador devem estar em português.
- Todas as listas paginadas devem manter filtros, `page`, `size` e `sort` na query string da rota.
- Confirmar teclado, foco visível, labels, mensagens com `role="alert"`, contraste e responsividade mobile/desktop.

## 2. Base e design system

### FE-001 — Fundações visuais

- Traduzir o design UI para tokens Tailwind: cores, tipografia, espaçamento, raios, sombras e breakpoints.
- Criar componentes em `src/shared/components/ui`: `BaseButton`, `BaseInput`, `BaseSelect`, `BaseTextarea`, `BaseModal`, `BaseDrawer`, `BaseBadge`, `BaseAvatar`, `BaseCard`, `BasePagination`, `BaseSkeleton`, `EmptyState` e `AlertMessage`.
- Suportar estados `disabled`, `loading`, `invalid`, foco, hover e variantes de perigo/sucesso.
- Criar Storybook apenas se a equipa decidir adotá-lo; não é requisito do backend.

### FE-002 — Estrutura da aplicação

- Finalizar `AppLayout` com header, navegação mobile, footer e menu do utilizador.
- Criar layouts separados quando necessário: `PublicLayout`, `AuthLayout`, `ClientLayout`, `ProviderLayout` e `AdminLayout`.
- Mostrar menus conforme `CLIENT`, `PROVIDER` ou `ADMIN`.
- Criar breadcrumbs, título de página e comportamento consistente de voltar.
- Criar páginas globais 403, 404, erro inesperado e indisponibilidade da API.

### FE-003 — Infraestrutura de dados

- Manter o cliente em `src/shared/api/client.ts` como único ponto HTTP.
- Preservar o refresh automático serializado: pedidos simultâneos não devem criar vários refreshes.
- Mapear `validationErrors` da API para os respetivos campos dos formulários.
- Criar composables comuns: `useAsyncState`, `usePagination`, `useDebounce`, `useConfirmDialog` e `useFilePreview`.
- Adicionar toast global para sucessos e erros não associados a um campo.
- Antes de produção, coordenar com o backend a migração do refresh token para cookie `HttpOnly`.

### FE-004 — Qualidade

- Adicionar ESLint, Prettier e regras para Vue + TypeScript.
- Adicionar Vitest e Vue Test Utils para componentes/stores.
- Adicionar Playwright para os fluxos críticos.
- Definir CI do frontend com install limpo, type-check, lint, testes e build.
- Cobrir pelo menos: login, refresh, guardas de rota, criação de booking e ações protegidas por role.

## 3. Autenticação e sessão

### FE-101 — Registo — `POST /auth/register`

- Finalizar `/register` com nome, apelido, e-mail, telefone opcional, palavra-passe, confirmação e escolha `CLIENT`/`PROVIDER`.
- Nunca permitir criação de `ADMIN` pela interface.
- Validar e-mail, limites de caracteres, palavra-passe entre 8 e 72 caracteres e confirmação igual.
- Mostrar erros por campo; no sucesso guardar a sessão e encaminhar cliente ao dashboard ou prestador ao onboarding profissional.

### FE-102 — Login — `POST /auth/login`

- Finalizar `/login`, incluindo mostrar/ocultar palavra-passe, loading e mensagem de credenciais inválidas.
- Respeitar `?redirect=` após autenticação; sem redirect, abrir o dashboard correto para o papel.
- Garantir que utilizadores suspensos/desativados veem a mensagem devolvida pela API.

### FE-103 — Renovação — `POST /auth/refresh`

- Não criar página. Validar o comportamento já implementado no cliente HTTP.
- Testar access token expirado, refresh válido, refresh inválido e vários pedidos simultâneos.
- Em falha, limpar sessão e redirecionar para login preservando a rota desejada.

### FE-104 — Recuperar palavra-passe — `POST /auth/forgot-password`

- Finalizar `/forgot-password` com e-mail e confirmação neutra para não revelar se a conta existe.
- Bloquear submissões repetidas durante o pedido e permitir regressar ao login.

### FE-105 — Redefinir palavra-passe — `POST /auth/reset-password`

- Finalizar `/reset-password?token=...` com nova palavra-passe e confirmação.
- Tratar token ausente, inválido ou expirado; no sucesso encaminhar para login.
- Nunca persistir o token de recuperação no storage.

### FE-106 — Logout local

- Como não existe endpoint de logout, limpar tokens e dados Pinia localmente.
- Redirecionar para home/login e impedir que dados privados permaneçam visíveis.

## 4. Utilizador atual

### FE-201 — Carregar conta — `GET /users/me`

- Usar no bootstrap da sessão, header, dashboard e página `/profile`.
- Mostrar nome, e-mail, telefone, role e status; nunca esperar ou mostrar `passwordHash`.

### FE-202 — Editar conta — `PATCH /users/me`

- Criar formulário para `firstName`, `lastName` e `phone`.
- Inicializar com os dados atuais, enviar apenas campos alterados e atualizar o store após sucesso.
- Criar área separada para palavra-passe; não inventar endpoint de troca de senha. Enquanto não existir, usar o fluxo de recuperação.

## 5. Prestadores

### FE-301 — Onboarding profissional — `POST /providers/profile`

- Disponível apenas para `PROVIDER` sem perfil.
- Criar passos: apresentação/bio, cidade, localização opcional, fotografia e revisão final.
- O endpoint inicial envia `bio`, `city`, `latitude` e `longitude`; upload da fotografia ocorre depois pelo endpoint de media.
- Explicar que latitude/longitude são opcionais e pedir permissão antes de usar geolocalização do browser.

### FE-302 — Perfil próprio — `GET /providers/me`

- Criar dashboard profissional com estado do perfil, avaliação, trabalhos concluídos e verificação.
- Se a API responder 404, encaminhar para onboarding.

### FE-303 — Editar perfil — `PATCH /providers/me`

- Permitir alterar bio, cidade e coordenadas.
- Não enviar `profileImageUrl`; a imagem usa upload dedicado.
- Atualizar imediatamente o perfil no store após sucesso.

### FE-304 — Solicitar verificação — `POST /providers/me/verification`

- Mostrar requisitos, confirmação e estado: `NOT_REQUESTED`, `PENDING`, `VERIFIED` ou `REJECTED`.
- Desativar nova solicitação quando pendente/verificado e mostrar nota administrativa quando rejeitada/revogada.

### FE-305 — Perfil público — `GET /providers/{id}`

- Criar `/providers/:id` com fotografia, nome, bio, cidade, selo de verificação, média, serviços e avaliações.
- Tratar prestador inexistente/inativo e usar skeleton durante carregamento.

### FE-306 — Pesquisa de prestadores — `GET /providers`

- Implementar `/providers` com `q`, `category`, `city`, `page`, `size` e `sort`.
- Debounce na pesquisa textual, filtros combináveis, limpar filtros e estado vazio.
- Preparar cartão para futura distância, sem calcular proximidade ainda.

## 6. Categorias

### FE-401 — Navegação pública — `GET /categories` e `GET /categories/{id}`

- Criar menu/grade hierárquica de categorias e subcategorias.
- Usar `slug` nos filtros de serviços/prestadores e `id` nos formulários de criação.
- Não mostrar categorias inativas; tratar categoria inexistente.

### FE-402 — Administração de categorias — `POST /admin/categories`

- Criar formulário ADMIN para nome e `parentId` opcional.
- Permitir criar categoria raiz ou subcategoria e atualizar a lista após sucesso.

### FE-403 — Editar categoria — `PATCH /admin/categories/{id}`

- Permitir alterar nome, parent e estado quando suportado pelo DTO.
- Evitar ciclos hierárquicos na interface e mostrar erros do backend.

### FE-404 — Desativar categoria — `DELETE /admin/categories/{id}`

- Usar diálogo de confirmação e explicar impacto nos serviços associados.
- Remover/marcar inativa na tabela apenas depois de `204`.

## 7. Serviços

### FE-501 — Catálogo — `GET /services`

- Evoluir a página existente com `q`, `category`, `city`, `minPrice`, `maxPrice`, `page`, `size` e `sort`.
- Sincronizar filtros com URL, validar intervalo de preço e oferecer ordenações compreensíveis.
- Criar cartões responsivos com prestador, categoria, cidade, preço `FIXED` ou `NEGOTIABLE` e imagem.

### FE-502 — Detalhe — `GET /services/{id}`

- Criar `/services/:id` com descrição, galeria, prestador, localização e CTA para pedir o serviço.
- Para visitante não autenticado, o CTA deve levar ao login/registo e regressar ao detalhe.

### FE-503 — Serviços do prestador — `GET /providers/{providerId}/services`

- Integrar na página pública do prestador com paginação.
- Reutilizar o cartão do catálogo e tratar lista vazia.

### FE-504 — Publicar serviço — `POST /services`

- Apenas `PROVIDER` com perfil profissional.
- Criar formulário com `categoryId`, título, descrição, `priceType` e preço.
- Para `FIXED`, preço é obrigatório e positivo; para `NEGOTIABLE`, ocultar/limpar preço conforme contrato.
- Após criação, encaminhar para gestão de imagens ou detalhe do serviço.

### FE-505 — Editar serviço — `PATCH /services/{id}`

- Carregar dados atuais, permitir edição parcial e enviar apenas campos alterados.
- Mostrar a ação apenas ao owner; tratar `403` mesmo assim.

### FE-506 — Desativar serviço próprio — `DELETE /services/{id}`

- Confirmar antes de desativar e atualizar a listagem somente após `204`.
- Explicar que a ação retira o serviço do catálogo público.

### FE-507 — Moderação de serviços — `GET /admin/services` e `DELETE /admin/services/{id}`

- Criar tabela ADMIN paginada incluindo serviços inativos.
- Permitir abrir detalhes do prestador/serviço e desativar conteúdo problemático com confirmação.

## 8. Media

### FE-601 — Fotografia do prestador — `POST /media/providers/profile-image`

- Criar seletor com preview, crop opcional e progresso de upload.
- Aceitar apenas JPEG, PNG ou WebP, máximo 5 MB; comunicar erros de conteúdo/dimensões.
- Enviar `multipart/form-data` no campo `file` e atualizar avatar com a URL devolvida.

### FE-602 — Imagens do serviço — `POST /media/services/{serviceId}/images`

- Apenas owner do serviço; permitir até 8 imagens e mostrar progresso individual.
- Validar formato/tamanho antes do envio, mantendo o backend como validação final.

### FE-603 — Galeria pública — `GET /media/services/{serviceId}/images`

- Integrar no detalhe e edição do serviço.
- Criar fallback quando não houver imagens; não existe endpoint de remoção/reordenação nesta fase.

## 9. Pedidos, agenda e pagamento

### FE-701 — Criar pedido — `POST /bookings`

- Apenas `CLIENT`. Criar fluxo a partir do detalhe do serviço.
- Enviar `serviceOfferingId`, data futura em ISO/UTC, endereço, notas opcionais e `CASH`/`MPESA`.
- Mostrar nomes amigáveis “Dinheiro” e “M-Pesa” e esclarecer que o LinkOps não processa o pagamento.
- Nunca solicitar PIN M-Pesa ou credenciais financeiras.

### FE-702 — Histórico — `GET /bookings`

- Criar `/bookings` paginado, adaptado ao papel: pedidos feitos para cliente e recebidos para prestador.
- Mostrar status, serviço, contraparte, data, pagamento e ações permitidas.
- Adicionar filtros visuais locais somente se forem suportados pela API ou claramente marcados como filtro da página atual.

### FE-703 — Detalhe — `GET /bookings/{id}`

- Criar `/bookings/:id`, acessível apenas às pessoas envolvidas/admin conforme API.
- Mostrar timeline: `PENDING`, `ACCEPTED`, `REJECTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.
- Atualizar os dados depois de cada ação e impedir duplo clique.

### FE-704 — Aceitar/rejeitar — `PATCH /bookings/{id}/accept`, `PATCH /bookings/{id}/reject`

- Apenas `PROVIDER` owner do pedido e somente no estado permitido.
- Pedir confirmação para rejeitar; refletir o novo estado e apresentar feedback.

### FE-705 — Iniciar/concluir — `PATCH /bookings/{id}/start`, `PATCH /bookings/{id}/complete`

- Apenas prestador envolvido, seguindo a sequência de estados.
- Para concluir, usar confirmação explícita porque a conclusão habilita avaliação.

### FE-706 — Cancelar — `PATCH /bookings/{id}/cancel`

- Apenas cliente envolvido e quando o status permitir.
- Confirmar a ação e explicar quando o backend rejeitar um cancelamento tardio.

### FE-707 — Registar pagamento — `PATCH /bookings/{id}/payment/paid`, `PATCH /bookings/{id}/payment/not-confirmed`

- Apenas prestador envolvido.
- Mostrar ação contextual para marcar `PAID` ou `NOT_CONFIRMED`; `PENDING` é o estado inicial.
- Isto é apenas registo manual: não criar checkout, gateway ou formulário financeiro.

## 10. Avaliações

### FE-801 — Avaliar booking — `POST /bookings/{bookingId}/review`

- Apenas cliente, booking `COMPLETED`, máximo uma avaliação.
- Criar rating acessível de 1 a 5 e comentário opcional com máximo 2000 caracteres.
- Após sucesso, retirar o CTA e atualizar média/lista do prestador.

### FE-802 — Avaliações públicas — `GET /providers/{providerId}/reviews`

- Listar com paginação na página do prestador.
- Mostrar rating, comentário, cliente e data; criar estado vazio.

## 11. Notificações

### FE-901 — Listar — `GET /notifications`

- Criar `/notifications` paginado e um resumo no header.
- Renderizar título/mensagem em português e ícone por tipo de notificação.
- Ligar `referenceId` ao booking/prestador relevante quando o tipo permitir.

### FE-902 — Marcar lida — `PATCH /notifications/{id}/read`

- Marcar individualmente, com atualização otimista e rollback em erro.
- Não inventar “marcar todas” enquanto não houver endpoint.
- Para badge de não lidas, calcular apenas sobre dados carregados ou solicitar um endpoint próprio; não assumir contagem global.

## 12. Administração

### FE-1001 — Utilizadores — `GET /admin/users`

- Criar tabela ADMIN paginada com nome, e-mail, role, status e datas.
- Adicionar pesquisa/filtros apenas quando suportados pelo backend; inicialmente usar paginação/sort.

### FE-1002 — Suspender/reativar — `PATCH /admin/users/{id}/suspend`, `PATCH /admin/users/{id}/reactivate`

- Mostrar apenas a ação válida para o status atual e pedir confirmação.
- Atualizar a linha com a resposta e exibir erros de regras administrativas.

### FE-1003 — Prestadores — `GET /admin/providers`

- Criar tabela ADMIN paginada com verificação, status, cidade, rating e datas de revisão.
- Destacar pedidos `PENDING` e permitir abrir o perfil público.

### FE-1004 — Verificação — `PATCH /admin/providers/{id}/verify`

- Confirmar aprovação e atualizar selo/status/notas com a resposta.

### FE-1005 — Rejeitar/revogar — `PATCH /admin/providers/{id}/reject-verification`, `PATCH /admin/providers/{id}/revoke-verification`

- Exigir motivo não vazio, mostrar confirmação e preservar a nota devolvida.
- “Rejeitar” aplica-se ao pedido pendente; “revogar” a um perfil já verificado.

### FE-1006 — Centro administrativo

- Unificar utilizadores, prestadores, serviços e categorias em `/admin` com rotas-filhas.
- Proteger todas com `roles: ['ADMIN']`, além da proteção obrigatória do backend.
- Não mostrar dados administrativos em caches persistentes do browser.

## 13. Funcionalidades sem endpoint dedicado

- `location`: usar cidade/latitude/longitude dentro de prestadores e serviços. Não implementar mapa de proximidade/PostGIS até existir contrato.
- `payment`: apresentar método/status dentro do booking; não há gateway nem recurso de pagamento separado.
- Contagem global de notificações, remoção de imagens, troca autenticada de palavra-passe, logout/revogação de token e favoritos ainda não têm endpoint. Não simular persistência para estes recursos sem decisão de produto/backend.

## 14. Ordem recomendada de entrega

1. FE-001 a FE-004: sistema visual, layouts, componentes comuns e testes.
2. FE-101 a FE-202: autenticação, recuperação e conta.
3. FE-401 e FE-501 a FE-503: descoberta pública.
4. FE-301 a FE-306 e FE-601: onboarding/perfil do prestador.
5. FE-504 a FE-506 e FE-602/603: publicação de serviços.
6. FE-701 a FE-707: bookings e registo de pagamento.
7. FE-801/802 e FE-901/902: avaliações e notificações.
8. FE-402 a FE-404, FE-507 e FE-1001 a FE-1006: administração.
9. Testes E2E, acessibilidade, performance, responsividade e revisão de segurança.

## 15. Definition of Done por tarefa

- UI igual ao design aprovado em mobile e desktop.
- Permissões e ownership refletidos na interface e confirmados pela API.
- Loading, vazio, erro e sucesso implementados.
- Validações do DTO reproduzidas para feedback rápido, sem substituir as do servidor.
- Query parameters preservados quando aplicável.
- Sem `any`, erros de TypeScript, logs temporários ou segredos.
- Testes unitários do comportamento crítico e E2E do fluxo quando aplicável.
- `npm run type-check`, lint, testes e `npm run build` passam.
- Textos em português, acessibilidade por teclado e revisão por outra pessoa da equipa.
