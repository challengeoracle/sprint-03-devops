# Checkpoint 05 - Medix (Sistema de Tickets para Unidades de Saúde)


## Vídeo Demonstrativo

Demonstração das funcionalidades, interface Thymeleaf e persistência dos dados no Azure SQL:

- [Link para o Vídeo no YouTube](https://youtu.be/I730krtsdEs)

---

## Integrantes do Grupo

- RM561061 - Arthur Thomas Mariano de Souza
- RM559873 - Davi Cavalcanti Jorge
- RM559728 - Mateus da Silveira Lima


## Descrição do Projeto
Este projeto apresenta um microserviço desenvolvido em Java Spring Boot para a gestão de chamados de suporte técnico (tickets) em unidades de saúde (clínicas e hospitais). A solução é integrada ao Azure SQL Database e permite que colaboradores registrem falhas técnicas em equipamentos ou sistemas, com monitoramento de prioridade e status.

## Benefícios da Implantação do Sistema

A implementação deste microserviço em unidades de saúde foca em:

- **Agilidade no Atendimento:** Reduz o tempo de resposta técnica para falhas em equipamentos críticos (ex: monitores de UTI), garantindo a continuidade do cuidado ao paciente.
- **Gestão de Prioridades:** Permite que a equipa de TI foque nos problemas de maior risco à operação hospitalar através da classificação de urgência.
- **Transparência Operacional:** Substitui processos manuais por um fluxo digital onde o status do chamado é acompanhado em tempo real por médicos e enfermeiros.
- **Decisões Baseadas em Dados:** Gera histórico para identificar equipamentos recorrentes em falhas, auxiliando no planeamento de manutenções preventivas.

---

## Menu de Navegação

- [Checkpoint 05 - Medix (Sistema de Tickets para Unidades de Saúde)](#checkpoint-05---medix-sistema-de-tickets-para-unidades-de-saúde)
    - [Menu de Navegação](#menu-de-navegação)
    - [Configuração Inicial da Infraestrutura Básica](#configuração-inicial-da-infraestrutura-básica)
        - [Executar no Azure Cloud Shell](#executar-no-azure-cloud-shell)
    - [Scripts de Estrutura do Banco de Dados (DDL)](#scripts-de-estrutura-do-banco-de-dados-ddl)
    - [Instruções de Carga e Massa de Testes (DML)](#instruções-de-carga-e-massa-de-testes-dml)
    - [Esteira Automatizada de DevOps (Estratégia Recomendada)](#esteira-automatizada-de-devops-estratégia-recomendada)
        - [1. Configuração do Web App Nativo para Docker](#1-configuração-do-web-app-nativo-para-docker)
        - [2. Configuração do Dockerfile](#2-configuração-do-dockerfile)
        - [3. Arquivo de Pipeline Azure DevOps](#3-arquivo-de-pipeline-azure-devops)
    - [Deploy Manual da Aplicação (Sem Pipeline - Legado)](#deploy-manual-da-aplicação-sem-pipeline---legado)
        - [Configuração do Web App Legado](#configuração-do-web-app-legado)
        - [Execução do Deploy Manual](#execução-do-deploy-manual)
    - [Documentação da API (Endpoints)](#documentação-da-api-endpoints)
        - [Exemplos de Requisição (JSON)](#exemplos-de-requisição-json)
    - [Vídeo Demonstrativo](#vídeo-demonstrativo)
    - [Integrantes do Grupo](#integrantes-do-grupo)

---

## Configuração Inicial da Infraestrutura Básica

### Executar no Azure Cloud Shell

1. Criar Grupo de Recursos
    ```bash
    az group create --name rg-medix-rm559728 --location southafricanorth
    ```

2. Criar o Servidor SQL
    ```bash
    az sql server create \
    --name sql-server-medix-rm559728 \
    --resource-group rg-medix-rm559728 \
    --location southafricanorth \
    --admin-user user-medix \
    --admin-password 'Fiap@2tdsvms' \
    --enable-public-network true
    ```

3. Criar o Banco de Dados (PaaS)
    ```bash
    az sql db create \
    --resource-group rg-medix-rm559728 \
    --server sql-server-medix-rm559728 \
    --name db-medix \
    --service-objective Basic \
    --backup-storage-redundancy Local \
    --zone-redundant false
    ```

4. Liberar Regras de Firewall do Banco de Dados
    ```bash
    az sql server firewall-rule create \
    --resource-group rg-medix-rm559728 \
    --server sql-server-medix-rm559728 \
    --name liberaGeral \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 255.255.255.255
    ```

5. Criar o Registro de Contêiner da Azure (Azure Container Registry - ACR)
    ```bash
    az acr create --resource-group rg-medix-rm559728 --name acrmedixrm559728 --sku Basic --admin-enabled true
    ```

---

## Scripts de Estrutura do Banco de Dados (DDL)

A criação das tabelas obedece ao relacionamento de 1 para N entre Colaboradores e Chamados. Para garantir a compatibilidade estrita com o mapeamento de entidades do JPA Hibernate, as tabelas devem ser geradas com iniciais em letra maiúscula através do **Query Editor** no portal da Azure:

```sql
-- Criação da Tabela de Colaboradores
CREATE TABLE Colaboradores (
    id_colaborador INT IDENTITY(1,1) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cargo VARCHAR(50),
    setor VARCHAR(50)
);

-- Criação da Tabela de Chamados de Suporte
CREATE TABLE Chamados (
    id_chamado INT IDENTITY(1,1) PRIMARY KEY,
    id_colaborador INT NOT NULL,
    descricao VARCHAR(MAX) NOT NULL,
    prioridade VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ABERTO',
    data_abertura DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_Colaborador_Chamado FOREIGN KEY (id_colaborador) REFERENCES Colaboradores(id_colaborador)
);
```

---

## Instruções de Carga e Massa de Testes (DML)

Para realizar testes funcionais na API e validar a persistência em Nuvem, execute o script abaixo utilizando as informações corporativas dos próprios integrantes do grupo no banco de dados:

```sql
-- Inserção da massa de dados inicial
INSERT INTO Colaboradores (nome, cargo, setor) VALUES 
('Mateus da Silveira Lima', 'Desenvolvedor Java', 'TI - Desenvolvimento'),
('Arthur Thomas Mariano de Souza', 'Desenvolvedor C#', 'TI - Desenvolvimento'),
('Davi Cavalcanti Jorge', 'Desenvolvedor Mobile', 'TI - Desenvolvimento');

-- Validação da carga efetuada
SELECT * FROM Colaboradores;
```

---

## Esteira Automatizada de DevOps (Estratégia Recomendada)

### 1. Configuração do Web App Nativo para Docker
Para rodar a esteira de CI/CD automatizada com contêineres, o Web App deve ser criado com suporte nativo a Linux e Docker no Azure Cloud Shell, além da otimização de tempo de carga do Spring Boot para evitar travamentos de inicialização:

```bash
# 1. Criar o Plano de Serviço em Linux
az appservice plan create --name plan-medix-rm559728 --resource-group rg-medix-rm559728 --location southafricanorth --sku F1 --is-linux

# 2. Criar o Web App baseado em container com imagem base temporária
az webapp create \
  --name web-medix-rm559728 \
  --resource-group rg-medix-rm559728 \
  --plan plan-medix-rm559728 \
  --deployment-container-image-name "nginx:latest"

# 3. Vincular credenciais do ACR e ajustar variáveis de ambiente e timeout de contêiner
az webapp config appsettings set --name web-medix-rm559728 --resource-group rg-medix-rm559728 --settings \
  SPRING_DATASOURCE_URL="jdbc:sqlserver://sql-server-medix-rm559728.database.windows.net:1433;database=db-medix;encrypt=true;trustServerCertificate=false;" \
  SPRING_DATASOURCE_USERNAME="user-medix" \
  SPRING_DATASOURCE_PASSWORD="Fiap@2tdsvms" \
  WEBSITES_PORT="8080" \
  WEBSITES_CONTAINER_START_TIME_LIMIT="1800"

# 4. Assegurar as diretivas administrativas ativas para o ACR antes da pipeline rodar
az acr update --name acrmedixrm559728 --admin-enabled true
```

### 2. Configuração do Dockerfile
Adicione um arquivo com o nome exato de `Dockerfile` localizado na raiz do projeto para o empacotamento do artefato da aplicação Java 21:

```dockerfile
FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080
ARG JAR_FILE=target/medixchamados-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 3. Arquivo de Pipeline Azure DevOps
Insira o arquivo `azure-pipelines.yml` na raiz do seu repositório de código do GitHub. Este modelo implementa uma esteira moderna dividida em dois estágios lógicos (*Multi-stage*): **Build (CI)** e **Deploy (CD)**.

```yaml
trigger:
  - main

resources:
  - repo: self

variables:
  azureSubscription: 'Conexao-Azure-DevOps' 
  acrName: 'acrmedixrm559728'                
  imageRepository: 'medix-api'
  webAppName: 'web-medix-rm559728'           
  dockerRegistryServiceConnection: 'Conexao-ACR' 
  tag: '$(Build.BuildId)'

stages:
  - stage: Build
    displayName: 'Estágio de Build e CI'
    jobs:
      - job: BuildJavaAndDocker
        displayName: 'Build JAR & Push Docker Image'
        pool:
          name: 'Default'
        steps:
          - task: JavaToolInstaller@0
            inputs:
              versionSpec: '21'
              jdkArchitectureOption: 'x64'
              jdkSourceOption: 'PreInstalled'

          - task: Maven@4
            displayName: 'Compilar aplicação com Maven'
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'clean package'
              options: '-DskipTests'
              publishJUnitResults: false
              javaHomeOption: 'JDKVersion'
              jdkVersionOption: '1.21'

          - task: Docker@2
            displayName: 'Construir e enviar imagem para o ACR'
            inputs:
              containerRegistry: '$(dockerRegistryServiceConnection)'
              repository: '$(imageRepository)'
              command: 'buildAndPush'
              Dockerfile: '**/Dockerfile'
              tags: |
                $(tag)
                latest

  - stage: Deploy
    displayName: 'Estágio de Deploy e CD'
    dependsOn: Build
    condition: succeeded()
    jobs:
      - job: DeployToAzure
        displayName: 'Deploy no Azure Web App'
        pool:
          name: 'Default'
        steps:
          - task: AzureWebAppContainer@1
            displayName: 'Deploy da Imagem Docker no Azure App Service'
            inputs:
              azureSubscription: '$(azureSubscription)'
              appName: '$(webAppName)'
              containers: '$(acrName).azurecr.io/$(imageRepository):$(tag)'
```

---

## Deploy Manual da Aplicação (Sem Pipeline - Legado)

### Configuração do Web App Legado
Caso opte por realizar a subida manual do binário bruto sem automações, configure a infraestrutura do Web App no Azure Cloud Shell com o runtime nativo do ecossistema Java:

```bash
# 1. Criar o Plano de Serviço legado
az appservice plan create --name plan-medix-rm559728 --resource-group rg-medix-rm559728 --location southafricanorth --sku F1 --is-linux

# 2. Criar o Web App apontando diretamente para o binário Java
az webapp create \
--name web-medix-rm559728 \
--resource-group rg-medix-rm559728 \
--plan plan-medix-rm559728 \
--runtime "JAVA|21-java21"

# 3. Configurar as variáveis de ambiente estruturadas para o datasource corporativo
az webapp config appsettings set --name web-medix-rm559728 --resource-group rg-medix-rm559728 --settings \
SPRING_DATASOURCE_URL="jdbc:sqlserver://sql-server-medix-rm559728.database.windows.net:1433;database=db-medix;encrypt=true;trustServerCertificate=false;" \
SPRING_DATASOURCE_USERNAME="user-medix" \
SPRING_DATASOURCE_PASSWORD="Fiap@2tdsvms"
```

### Execução do Deploy Manual

1. Executar a geração local do artefato compactado `.jar` na raiz do seu projeto:
    ```bash
    mvnw clean package -DskipTests
    ```

2. Efetuar o upload manual do arquivo binário `.jar` gerado na pasta `target/` para a infraestrutura da Azure utilizando o terminal do console de gerenciamento.

3. Inicializar o gatilho de deploy manual via Azure Cloud Shell:
    ```bash
    az webapp deploy \
    --resource-group rg-medix-rm559728 \
    --name web-medix-rm559728 \
    --src-path medixchamados-0.0.1-SNAPSHOT.jar \
    --type jar
    ```

---

## Documentação da API (Endpoints)

A URL base para a API é:
```bash
https://web-medix-rm559728.azurewebsites.net/api
```

A interface visual está disponível em:
```bash
https://web-medix-rm559728.azurewebsites.net/
```
| Método     | Endpoint         | Descrição                                        |
| :--------- |:-----------------|:-------------------------------------------------|
| **GET**    | `/chamados`      | Lista todos os tickets de suporte.               |
| **POST**   | `/chamados`      | Abre um novo chamado vinculado a um colaborador. |
| **PUT**    | `/chamados/{id}` | Atualiza o status ou prioridade de um ticket.    |
| **DELETE** | `/chamados/{id}` | Remove um chamado permanentemente.               |
| **POST**   | `/colaboradores` | Cadastra um novo colaborador no sistema.         |
| **GET**    | `/colaboradores` | Lista todos os colaboradores.                    |

### Exemplos de Requisição (JSON)

**POST /api/chamados**

```json
{
    "idColaborador": 1,
    "descricao": "Monitor de sinais vitais do leito 04 sem conexão com a rede.",
    "prioridade": "ALTA"
}
```

**PUT /api/chamados/1**

```json
{
    "status": "EM_ATENDIMENTO",
    "prioridade": "ALTA"
}
```

---

