# Final Project - Screenly

**Course:** Desenvolvimento de Aplicações Móveis (DAM) <br>
**Student:** Carolina Raposo (n.º 51568) <br>
**Date:** 14/06/2026 <br>
**Repository URL:** 

---

## 1. Introduction

Este projeto foi desenvolvido no âmbito da unidade curricular de Desenvolvimento de Aplicações
Móveis e consiste numa aplicação Android denominada **Screenly**, cujo objetivo é permitir ao 
utilizador catalogar filmes e séries, gerir listas de visualização e partilhar a sua opinião sobre 
os títulos que viu.

A principal inspiração para o desenvolvimento da Screenly foi a plataforma `Letterboxd`, uma rede 
social dedicada a pessoas que desfrutam de filmes e séries, permitindo que estes registem e avaliem 
filmes e criar listas de visualização. A **Screenly** adapta este conceito para o contexto móvel Android,
focando-se na gestão pessoal de conteúdos audiovisuais.

Este projeto representa a junção de todos os conhecimentos adquiridos ao longo do semestre. Num ponto 
de vista técnico, a aplicação foi desenvolvida em **Kotlin**, utilizando **Jetpack Compose** para a 
interface gráfica dos ecrãs principais e **XML** para os ecrãs de autenticação, **Retrofit** para a 
comunicação com a API do TMDb e **Firebase** (Authentication e Firestore) para a autenticação e persistência
de dados na nuvem. A arquitetura seguida foi o padrão MVVM (*Model-View-ViewModel*), garantindo uma separação 
clara entre lógica de negócio e a interface gráfica.

## 2. System Overview

A **Screenly** é uma aplicação móvel Android que permite aos utilizadores descobrir, catalogar e avaliar
filmes e séries. A aplicação consome a API pública do TMDb para obter informação atualizada sobre conteúdos
audiovisuais, e recorre ao Firebase para gerir a autenticação e persistir os dados pessoais de cada utilizador
na nuvem.

A aplicação esté organizada em cinco áreas principais, acessíveis por uma barra de navegação inferior:

- **Início**: apresenta os títulos em tendência da semana, os filmes mais populares e séries mais populares;
- **Pesquisa**: permite pesquisar filmes e séries por nome, ou explorar conteúdos por filtros, como, por exemplo,
por género e país de origem.
- **Detalhes**: ao selecionar um título, o utilizador acede à sua sinopse, elenco, *trailer* oficial, títulos
semelhantes e *reviews* da comunidade TMDb. É também neste ecrã que o utilizador pode adicionar o título a uma  
das duas listas e atribuir uma classificação e *review* pessoal. 
- **Listas**: apresenta os títulos guardados pelo utilizador organizados em três categorias, tais como *To Watch*,
*Watching* e *Watched*;
- **Perfil**: apresenta a informação do utilizador autenticado, o acesso às suas listas e as definições da aplicação,
incluindo a opção de terminar a sessão.

Os principais casos de uso da aplicação são:

- Registar e autenticar utilizadores;
- Pesquisar e explorar filmes e séries;
- Consultar informação detalhada sobre um título;
- Gerir listas pessoais de visualização;
- Avaliar e escrever *reviews* sobre títulos já vistos.

## 3. Architecture and Package Organization

A aplicação segue o padrão de arquitetura MVVM (*Model-View-ViewModel*), recomendado pela Google para 
aplicações Android modernas. Este padrão garante uma separação clara entre a lógica de negócio e a 
interface gráfica, facilitando a manutenção e escalabilidade do código

### 3.1 Estrutura de Packages
```
dam_a51568.screenly/
├── data/
│   ├── model/
│   │   ├── CastMember.kt
│   │   ├── Country.kt
│   │   ├── CrewMember.kt
│   │   ├── Genre.kt
│   │   ├── MediaItem.kt
│   │   ├── Movie.kt
│   │   ├── Review.kt
│   │   ├── TvShow.kt
│   │   ├── User.kt
│   │   ├── WatchlistItem.kt
│   │   └── WatchStatus.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── TmdbApiService.kt
│   │   ├── dto/
│   │   │   └── TmdbDtos.kt
│   │   └── TmdbClient.kt
│   └── repository/
│       ├── AuthMappers.kt
│       ├── MediaMappers.kt
│       ├── UserRepository.kt
│       └── WatchlistRepository.kt
├──  ui/
│    ├── auth/
│    ├── browse/
│    ├── details/
│    ├── home/
│    ├── lists/
│    ├── profile/
│    ├── search/
│    ├── settings/
│    ├── theme/
└── MainActivity.kt
```
### 3.2 Descrição das Camadas

#### Camada de Dados (`data/`)

A camada de dados és responsável por toda a lógica de acesso e manipulação de dados, estando organizada
em três *subpackages*:

- **`model/`**: contém os modelos de domínio da aplicação, ou seja, os objetos que sã usados internamente
pela *app*. Inclui modelos como `Movie`, `TvShow`, `WatchlistItem`, `User`, `CastMember`, `CrewMember`,
`Genre`, `Country`, `MediaItem` e o enum `WatchStatus` que representa os três estados possíveis de um título
na *watchlist* (*To Watch*, *Watching*, *Watched*);
- **`remote/`**: contém tudo o que diz respeito à comunicação com a API do TMDb. O *subpackage* `api/`
contém a interface **`TmdbApiService**`** com todos os *endpoints* Retrofit, o *subpackage* `dto` contém os
objetos de transferência de dados (*Data Transfer Objects*) que espelham a estrutura da resposta da API, e o
**`TmbdClient`** é o singleton responsável por configurar e fornecer a instância do Retrofit.
- **`repository/`**: contém os repositórios e os mappers. O **`WatchlistRepository`** gere as operações 
de leitura e escrita na coleção *watchlist* do Firestore, o **`UserRepository`** gere os dados do utilizador,
o **`MediaMapper`** contém as funções de extensão que convertem os DTOs da API nos modelos de domínio, e o **`AuthMappers`** converte o objeto `FirebaseUser` no modelo `User`.

#### Camada de Interface (`ui/`)

A camada de interface contém todos os ecrãs da aplicação, organizados por funcionalidade. Cada pasta contém o
ecrã em Jetpack Compose e o respetivo ViewModel:

- **`auth/`**: ecrãs de autenticação, desenvolvidos em XML com View Binding;
- **`home/`**: ecrã principal com títulos em tendência e populares;
- **`search7`**: ecrã de pesquisa com filtros por género e país;
- **`details/`**: ecrã de detalhes de um título;
- **`browse/`**: ecrãs de navegação por género, país e resultados filtrados;
- **`lists/`**: ecrã de listagem detalhada por estado;
- **`profile/`**: ecrã de perfil do utilizador; 
- **`setting`**; ecrã de definições com opção de terminar sessão;
- **`theme`**: definição da cor da aplicação.

**Porquê esta estrutura?** Esta organização por camadas impõe uma regra de dependência unidirecional, ou seja,
a camada de interface (`ui/`) depende dos modelos de domínio (`model/`), e a camada de dados (`data/`) é
responsável por fornecer esses modelos através de repositórios. Com esta separação de responsabilidades, a interface
gráfica nunca é lida diretamente com lógica de rede ou protocolos HTTP, sendo que essa responsabilidade pertence 
exclusivamente à camada de dados, nomeadamente ao **`TmdbClient`** e ao **`TmdbApiService`** para a comunicação com a API, 
e aos repositórios para a gestão dos dados no Firestore.

Uma vantagem prática desta abordagem é a flexibilidade que oferece para alterações futuras. Por exemplo, se no futuro
fosse necessário migrar o armazenamento de dados do **Firebase Firestore** para outra base de dados, as alterações
seriam feitas apenas no *package* `data/`, sem que fosse necessário modificar uma única linha de código na camada
de interface. Da mesma forma, se a API do TMDb fosse substituída por outra fonte de dados, apenas os ficheiros em
`data/remote/` seriam afetados.

## 4. Implementation and Dependencies

### 4.1 Autenticação

A autenticação da aplicação foi implementada recorrendo ao **Firebase Authentication**, que gere o registo e o *login*
dos utilizadores através de email e palavra-passe. Os ecrãs de autenticação foram desenvolvidos em **XMl** com **View Biding**.

O ecrã de registo valida os campos introduzidos pelo utilizador antes de enviar o pedido ao Firebase, verificando se o email
tem um formato válido, se a palavra-passe tem pelo menos seis caracteres e se a confirmação da palavra-passe coincide com a 
palavra-passe introduzida. Após o registo com sucesso, o nome do utilizador é guardado no perfil do Firebase Authentication e os
dados do utilizador são persistidos no Firestore através do **UserRepository**.

O ecrã de *login* verifica, ao iniciar, se já existe uma sessão ativa. Caso exista, o utilizador é redirecionado diretamente para o
ecrã principal, evitando que tenha de autenticar novamente sempre que abre a aplicação. Em caso de erro, são apresentadas mensagens
específicas consoante o tipo de exceção devolvida pelo Firebase, como, por exemplo, conta inexistente ou credenciais incorretas.

### 4.2 Comunicação com a API do TMDb

A comunicação com a API do TMDb foi implementada através do Retrofit, uma biblioteca HTTP para Android que simplifica o consumo 
de APIs REST. A interface **TMDbApiService** define todos os *endpoints* utilizados na aplicação, incluindo pesquisa de títulos, 
detalhes de filmes e séries, créditos, *trailers*, *reviews* e títulos semelhantes. Todos os métodos são declarados como **suspend functions**,
permitindo que sejam chamados dentro de coroutines sem bloquear a *thread* principal.

O **TmdbClient** é um singleton responsável por configurar a instância do Retrofit, definindo a URL base da API e o conversor Gson
para deserializar as respostas JSON. A chave de autenticação da API é lida de forma segura a partir do `BuildConfig`, que por sua
vez a obtém do ficheiros `local.properties`, evitando a exposição de informação sensível no código fonte.

As respostas da API sã mapeadas em objetos DTO (*Data Transfer Objects*) que espelham a estrutura JSON devolvida pela API. Estes DTOs
são posteriormente convertidos pelos modelos de domínio da aplicação através das funções de extensão definidas
no **MediaMappers**, garantindo que a interface gráfica nunca depende diretamente da estrutura da API.

### 4.3 Camada de Dados

A camada de dados está organizada de forma a separar os objetos que vêm da API dos objetos utilizados internamente pela
aplicação. Os modelos de domínio representam os conceitos centrais da aplicação, como **`Movie`**, **`TvShow`**, **`WatchlistItem`**,
**`User`**, **`CastMember`**, **`CrewMember`**, **`Review`**, **`Genre`** e **`Country`**. Estes modelos são independentes de qualquer
fonte de dados externa, o que significa que não contêm anotações do Retrofit ou do Firestore.

A conversão entre DTOs e modelos de domínio é feita através de funções de extensão definidas no **MediaMappers**. Por exemplo, quando a
API devolve um `TmdbMovieDetails`, este é convertido num `Movie` através da função **toMovie()**, que constrói o URL completo do póster,
extrai o ano de lançamento e mapeia os géneros para uma lista de *strings*. Esta abordagem garante que os ViewModels e a interface gráfica 
trabalham sempre com objetos limpos e independentes da fonte de dados.

### 4.4 Persistência com Firestore

A persistência dos dados pessoais do utilizador foi implementada com o Firebase Firestore, uma base de 
dados NoSQL na nuvem. Os dados estão organizados numa estrutura hierárquica onde cada utilizador tem o
seu próprio documento na coleção `users`, com uma subcoleção `watchlist` que contém os títulos guardados.

O **`WatchlistRepository`** utiliza Flow com **callbackFlow** para observar as alterações na base de dados em 
tempo real. Quando o utilizador adiciona, remove ou atualiza um título na watchlist, o Firestore notifica
automaticamente a aplicação e a interface gráfica é atualizada sem necessidade de recarregar os dados 
manualmente. As operações de escrita, como adicionar ou remover um título, são declaradas como 
**`suspend functions`** e utilizam **.await()** para aguardar a conclusão da operação de forma assíncrona.

As regras de segurança do Firestore foram configuradas para garantir que cada utilizador só 
consegue aceder e modificar os seus próprios dados, impedindo o acesso não autorizado aos dados de 
outros utilizadores.

### 4.5 Interface Gráfica

A interface gráfica foi desenvolvida maioritariamente em **Jetpack Compose**, a *framework* declarativa
moderna recomendada pela Google para o desenvolvimento de interfaces Android. Cada ecrã tem o seu
próprio **ViewModel** que expõe o estado da UI através de **`StateFlow`**, e o Compose observa essas alterações 
automaticamente através do `collectAsState()`, redesenhando apenas os componentes afetados pela mudança
de estado.

A navegação entre ecrãs foi implementada com o **Navigation Compose**, que define um grafo de navegação com 
todas as rotas da aplicação. A barra de navegação inferior permite ao utilizador navegar entre os ecrãs
principais, **Início**, **Pesquisa** e **Perfil**, enquanto os ecrãs secundários, como os detalhes de um título 
ou as listas, são acedidos através de navegação direta com passagem de argumentos.

O carregamento de imagens, nomeadamente os pósters dos filmes e séries, foi implementado com a biblioteca
**Coil**, que gere automaticamente o carregamento assíncrono e a cache das imagens a partir dos URLs 
fornecidos pela API do TMDb.

## 5. Testing and Validation

### 5.1 Estratégia de Testes

A validação da aplicação foi realizada através de testes manuais no emulador do Android Studio. Durante o 
desenvolvimento, a aplicação foi testada iterativamente à medida que cada funcionalidade era implementada, 
verificando o comportamento esperado em cada ecrã.

#### 5.2 Cenários Testados

Os principais cenários validados manualmente foram:

- **Autenticação**: registo de um novo utilizador, *login* com credenciais válidas e redirecionamento 
automático para o ecrã principal caso já exista uma sessão ativa;
- **Pesquisa**: pesquisa de filmes e séries por nome, aplicação de filtros por género 
e país e navegação para os detalhes de um título a partir dos resultados;
- **Detalhes**: consulta da sinopse, elenco, *trailer* e títulos similares de um filme ou série;
- **Watchlist**: adição de títulos às três listas (To Watch, Watching e Watched), 
alteração de estado e remoção de títulos;
- **Avaliação pessoal**: atribuição de classificação e escrita de *review* para títulos no estado Watched;
- **Persistência**: verificação de que os dados guardados na watchlist persistem após fechar e reabrir
a aplicação, confirmando a sincronização com o Firestore.

### 5.3 Limitações Conhecidas

Durante o desenvolvimento e validação da aplicação foram identificadas as seguintes limitações:

- Informação incompleta — nem todos os títulos disponíveis na API do TMDb têm informação completa. 
Alguns filmes e séries não têm sinopse disponível, sendo apresentada a mensagem "Sinopse
não disponível". Da mesma forma, a secção de reviews da comunidade pode estar vazia ou conter
apenas uma review para determinados títulos, sendo que esta limitação é inerente à
própria base de dados do TMDb e não à aplicação;

## 6. Usage Instructions

### 6.1 Pré-requesitos

- Android Studio
- Android SDK
- JDK
- Um projeto Firebase (Autenticação e Firestore)

### 6.2 Configuração do Projeto

1. Clonar o repositório.
2. Abrir o projeto no Android Studio
3. Adicionar o ficheiro google-services.json em `app/`
4. Configurar o Firebase:
    - Ativar a autenticação por email/palavra-passe
    - Criar uma base de dados Firestore
5. Sincronizar o Gradle
6. Compilar e executar a partir do Android Studio num emulador ou dispositivo físico

### 6.3 Observações 

- O Gradle Wrapper referencia atualmente versões de plugins/dependências que podem não ser resolvidas 
em todos os ambientes.
- Se a resolução de dependências falhar alinhe as versões com as versões estáveis disponíveis no momento.

## 7. Difficulties and Lessons Learned

### 7.1 Dificuldades

#### Organização dos *packages*

Uma das principais dificuldades encontradas ao longo do desenvolvimento foi a organização correta dos packages
do projeto. Inicialmente, os modelos de domínio e os DTOs da API encontravam-se misturados num único ficheiro, sem uma
separação clara entre os objetos que vinham da API e os objetos utilizados internamente pela aplicação. Perceber a 
diferença entre um DTO (*Data Transfer Object) e um modelo de domínio, e compreender onde cada ficheiro devia ser colocado, 
foi um processo gradual que exigiu uma reestruturação do projeto.

#### Migração para o Firebase Firestore

Outra dificuldade foi a migração do armazenamento em memória RAM para o **Firebase Firestore**. Inicialmente, os dados
da watchlist eram guardados num `mutableStateListOf` em memória, o que significava que os dados se perdiam sempre que a 
aplicação era fechada. A migração para o Firestore implicou compreender como estruturar os dados numa base de dados NoSQL,
como configurar as regras de segurança e como observar as alterações em tempo real utilizado `callbackFlow`. 

### 7.2 Lições Aprendidas

O desenvolvimento da Screenly permitiu consolidar e profundar vários conhecimentos:

- **Arquitetura MVVM**: a aplicação prática do padrão MVVM permitiu compreender a importância de separar a lógica de negócio 
da interface gráfica, tornando o código mais organizado e fácil de manter;
- **Separação entre DTOs e modelos de domínio**: perceber que os objetos vêm da API não devem ser usados diretamente
pela interface gráfica, e que os mappers são a forma correta de fazer essa conversão;
- **Jetpack Compose**: o desenvolvimento da interface com Jetpack Compose permitiu compreender o paradigma declarativo 
e como o Compose observa e rage automaticamente às alterações de estado através dos StateFlow;
- **Firebase**: a integração com o Firebase Authentication e o Firestore permitiu compreender como funciona a autenticação na 
- nuvem e como persistir dados de forma segura e em tempo real numa aplicação móvel.

## 8. Future Improvements

Embora a aplicação Screenly implemente as funcionalidades essenciais para a gestão de conteúdos audiovisuais, existem
diversas melhorias e extensões que poderiam ser implementadas futuramente:

- **Suporte multilingue**: atualmente a aplicação está disponível apenas em português. Uma melhoria relevante seria
adicionar para múltiplos idiomas, nomeadamente inglês, recorrendo ao sistema de localização do Android (`strings.xml`) para que 
a aplicação se adapte automaticamente ao idioma do dispositivo do utilizador;
- **Funcionalidades sociais**: à semelhança da plataforma que serviu de inspiração, o Letterboxd, seria interessante
implementar funcionalidades sociais com a possibilidade de seguir outros utilizadores, consultar as suas listas e *reviews*, e 
descobrir novos títulos com base nas recomendações da comunidade;
- **Modo *offline***: implementar uma cache local com **Room Database** para permitir que o utilizador consulte a sua 
watchlist mesmo sem ligação à internet.

## 9. AI Usage Disclosure

No desenvolvimento deste projeto e na elaboração do presente relatório foi utilizado ferramentas de inteligência artificial, 
sendo que estas foram utilizadas para:

- Apoio na definição reestruturação da arquitetura do projeto e organização dos packages;
- Esclarecimento de dúvidas sobre boas práticas de desenvolvimento Android moderno;
- Identificação e resolução de erros ao longo do desenvolvimento;
- Redação e estruturação do presente relatório.

O estudante declara que todas as decisões técnicas e funcionais foram tomadas de forma autónoma, sendo que as ferramentas de IA 
foram utilizadas exclusivamente como apoio ao desenvolvimento e à aprendizagem. O estudante assume total responsabilidade pelo
conteúdo do projeto e do presente relatório.
