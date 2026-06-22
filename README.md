# DSCommerce API

> API REST de e-commerce desenvolvida em **Java 21 + Spring Boot 3**, com autenticação **OAuth2 / JWT**, controle de acesso baseado em papéis (RBAC), paginação, validação de dados e tratamento centralizado de exceções.

Projeto backend que modela o domínio de uma loja virtual — produtos, categorias, usuários e pedidos — expondo uma API REST segura e pronta para ser consumida por um front-end (web ou mobile).

---

## 📑 Índice

- [Sobre o projeto](#-sobre-o-projeto)
- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Modelo de domínio](#-modelo-de-domínio)
- [Segurança e autenticação](#-segurança-e-autenticação)
- [Endpoints da API](#-endpoints-da-api)
- [Como executar](#-como-executar)
- [Usuários de teste](#-usuários-de-teste)
- [Exemplos de requisição](#-exemplos-de-requisição)
- [Aprendizados e destaques técnicos](#-aprendizados-e-destaques-técnicos)
- [Autor](#-autor)

---

## 💡 Sobre o projeto

O **DSCommerce** é uma API de catálogo e gestão de pedidos de uma loja virtual. O sistema permite:

- Consultar produtos com **paginação** e **busca por nome**;
- Gerenciar o catálogo (criar, atualizar e remover produtos) — restrito a administradores;
- Listar categorias;
- Registrar pedidos vinculados ao usuário autenticado;
- Consultar dados do próprio usuário logado;
- Autenticação via **OAuth2 Password Grant** com emissão de **JWT** assinado.

Cada recurso possui regras de autorização específicas baseadas nos papéis do usuário (`ADMIN` e `OPERATOR`).

---

## 🛠 Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.3 |
| Persistência | Spring Data JPA / Hibernate |
| Segurança | Spring Security + OAuth2 Authorization Server + Resource Server (JWT) |
| Validação | Bean Validation (Jakarta Validation) |
| Banco de dados | H2 (em memória, perfil de testes) |
| Build | Maven |

---

## 🏗 Arquitetura

O projeto segue uma arquitetura em camadas, separando responsabilidades de forma clara:

```
controllers   →  Camada REST: recebe requisições, retorna ResponseEntity
   ↓
services      →  Regras de negócio, validações e orquestração
   ↓
repositories  →  Acesso a dados (Spring Data JPA)
   ↓
entities      →  Mapeamento objeto-relacional (JPA)
```

Outros pacotes relevantes:

- **`dto`** — objetos de transferência de dados, isolando as entidades da camada de exposição e carregando as anotações de validação (ex.: `ProductDTO`, `OrderDTO`, `UserDTO`).
- **`config`** — configuração do *Authorization Server*, *Resource Server*, CORS e o *custom grant* de senha.
- **`controllers/handlers`** — `ControllerExceptionHandler` com `@ControllerAdvice` para tratamento centralizado de erros.
- **`services/exceptions`** — exceções de negócio (`ResourceNotFoundException`, `DatabaseException`, `ForbiddenException`).
- **`projections`** — projeções para consultas otimizadas (ex.: carregamento de usuário e papéis na autenticação).

---

## 🗂 Modelo de domínio

```
User ───< Order ───< OrderItem >─── Product >───< Category
 │                       │
 │                    Payment (1:1 com Order)
 └──< Role (N:N)
```

- **User** implementa `UserDetails` e possui um conjunto de **Roles** (papéis).
- **Order** pertence a um **User** (cliente), possui **status** (`OrderStatus`), um **Payment** opcional e vários **OrderItem**.
- **OrderItem** usa chave composta (`OrderItemPK`) entre `Order` e `Product`, guardando quantidade e preço no momento da compra.
- **Product** relaciona-se com **Category** em N:N.

**Status do pedido:** `WAITING_PAYMENT`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELED`.

---

## 🔐 Segurança e autenticação

A API utiliza **OAuth2 Password Grant** com tokens **JWT**:

- O **Authorization Server** emite o token a partir de `client_id`/`client_secret` + credenciais do usuário.
- O **Resource Server** valida o JWT em cada requisição e extrai as *authorities* do claim `authorities`.
- A autorização por endpoint é aplicada via `@PreAuthorize` com `@EnableMethodSecurity`.
- **CORS** configurável por variável de ambiente.

**Papéis:**

| Papel | Permissões |
|---|---|
| `ROLE_ADMIN` | Gerenciar produtos (criar/editar/excluir) e consultar pedidos |
| `ROLE_OPERATOR` | Criar pedidos e consultar o próprio perfil |

Parâmetros configuráveis (via variáveis de ambiente, com valores padrão):

| Variável | Padrão | Descrição |
|---|---|---|
| `CLIENT_ID` | `myclientid` | ID do client OAuth2 |
| `CLIENT_SECRET` | `myclientsecret` | Secret do client OAuth2 |
| `JWT_DURATION` | `86400` | Duração do token (segundos) |
| `CORS_ORIGINS` | `http://localhost:3000,http://localhost:5173` | Origens permitidas |

---

## 📡 Endpoints da API

### Produtos `/products`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/products` | Público | Lista paginada (filtro opcional `?name=`) |
| `GET` | `/products/{id}` | Público | Busca produto por ID |
| `POST` | `/products` | `ADMIN` | Cria um produto |
| `PUT` | `/products/{id}` | `ADMIN` | Atualiza um produto |
| `DELETE` | `/products/{id}` | `ADMIN` | Remove um produto |

### Categorias `/categories`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/categories` | Público | Lista todas as categorias |

### Pedidos `/orders`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/orders/{id}` | `ADMIN`, `OPERATOR` | Busca pedido por ID |
| `POST` | `/orders` | `OPERATOR` | Cria um pedido |

### Usuários `/users`

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/users/me` | `ADMIN`, `OPERATOR` | Dados do usuário autenticado |

---

## ▶ Como executar

### Pré-requisitos
- Java 21+
- Maven (ou use o wrapper `./mvnw` incluso)

### Passos

```bash
# clonar o repositório
git clone https://github.com/marinsJava/dscommerce.git
cd dscommerce

# executar a aplicação
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080` com o perfil `test`, usando banco **H2 em memória** populado automaticamente pelo `import.sql`.

**Console do H2:** `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:testdb` · usuário: `sa` · sem senha)

---

## 👤 Usuários de teste

Carregados automaticamente no início (senha de ambos: **`123456`**):

| E-mail | Papéis |
|---|---|
| `maria@gmail.com` | `OPERATOR` |
| `alex@gmail.com` | `OPERATOR`, `ADMIN` |

---

## 🧪 Exemplos de requisição

### 1. Obter o token de acesso

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -u myclientid:myclientsecret \
  -d "grant_type=password" \
  -d "username=alex@gmail.com" \
  -d "password=123456"
```

Resposta (resumida):

```json
{
  "access_token": "eyJraWQiOi...",
  "token_type": "Bearer",
  "expires_in": 86399
}
```

### 2. Consultar produtos (público)

```bash
curl "http://localhost:8080/products?name=gamer&page=0&size=12"
```

### 3. Criar um produto (requer ADMIN)

```bash
curl -X POST http://localhost:8080/products \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
        "name": "Teclado Mecânico",
        "description": "Teclado mecânico RGB switch blue",
        "price": 350.00,
        "imgUrl": "https://exemplo.com/teclado.jpg",
        "categories": [{ "id": 3 }]
      }'
```

> Os campos passam por **Bean Validation** — ex.: `name` entre 3 e 80 caracteres, `description` com no mínimo 10, `price` positivo e ao menos uma categoria associada. Erros retornam `422 Unprocessable Entity` com a lista de campos inválidos.

---

## 🎯 Aprendizados e destaques técnicos

Este projeto consolida diversos conceitos importantes de back-end com Spring:

- ✅ **Spring Security + OAuth2** com *Authorization Server* e *Resource Server* na mesma aplicação;
- ✅ **JWT** customizado com claims de *authorities* e *custom password grant*;
- ✅ **Autorização por método** com `@PreAuthorize` e RBAC;
- ✅ **DTO Pattern** para desacoplar entidades da API;
- ✅ **Bean Validation** com mensagens customizadas;
- ✅ **Tratamento global de exceções** com `@ControllerAdvice` e respostas padronizadas;
- ✅ **Paginação e filtros** com `Pageable`;
- ✅ Modelagem JPA avançada: **chave composta**, relacionamentos **1:1**, **1:N** e **N:N**;
- ✅ **CORS** e configuração via variáveis de ambiente, pronta para deploy.

---

## ✍ Autor

**Marins** · [GitHub @marinsJava](https://github.com/marinsJava)

---

<p align="center">⭐ Se este projeto te ajudou ou te interessou, deixe uma estrela!</p>
