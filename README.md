# Checkpoint 05 - Medix (Sistema de Tickets para Unidades de Saúde)

## Vídeo Demonstrativo
Demonstração das funcionalidades, interface Thymeleaf e persistência dos dados no Azure SQL:
- [Link para o Vídeo no YouTube](https://youtu.be/I730krtsdEs)

---

## Integrantes do Grupo
- RM561061 - Arthur Thomas Mariano de Souza
- RM559873 - Davi Cavalcanti Jorge
- RM559728 - Mateus da Silveira Lima

---

## Descrição do Projeto
Este projeto apresenta um microserviço desenvolvido em Java Spring Boot para a gestão de chamados de suporte técnico (tickets) em unidades de saúde (clínicas e hospitais). A solução é integrada ao Azure SQL Database e permite que colaboradores registrem falhas técnicas em equipamentos ou sistemas, com monitoramento de prioridade e status.

## Benefícios da Implantação do Sistema
- **Agilidade no Atendimento:** Reduz o tempo de resposta técnica para falhas em equipamentos críticos (ex: monitores de UTI), garantindo a continuidade do cuidado ao paciente.
- **Gestão de Prioridades:** Permite que a equipa de TI foque nos problemas de maior risco à operação hospitalar através da classificação de urgência.
- **Transparência Operacional:** Substitui processos manuais por um fluxo digital onde o status do chamado é acompanhado em tempo real por médicos e enfermeiros.
- **Decisões Baseadas em Dados:** Gera histórico para identificar equipamentos recorrentes em falhas, auxiliando no planeamento de manutenções preventivas.

---

## Menu de Navegação
- [Configuração Inicial da Infraestrutura Básica](#configuração-inicial-da-infraestrutura-básica)
- [Scripts de Estrutura do Banco de Dados (DDL)](#scripts-de-estrutura-do-banco-de-dados-ddl)
- [Instruções de Carga e Massa de Testes (DML)](#instruções-de-carga-e-massa-de-testes-dml)
- [Configuração das Conexões no Azure DevOps](#configuração-das-conexões-no-azure-devops)
- [Esteira Automatizada de DevOps](#esteira-automatizada-de-devops)
- [Deploy Manual da Aplicação (Sem Pipeline - Legado)](#deploy-manual-da-aplicação-sem-pipeline---legado)
- [Documentação da API (Endpoints)](#documentação-da-api-endpoints)

---

## Configuração Inicial da Infraestrutura Básica

### Executar no Azure Cloud Shell

1. Criar Grupo de Recursos
   @@@bash
   az group create --name rg-medix-rm559728 --location southafricanorth
   @@@

2. Criar o Servidor SQL
   @@@bash
   az sql server create \
   --name sql-server-medix-rm559728 \
   --resource-group rg-medix-rm559728 \
   --location southafricanorth \
   --admin-user user-medix \
   --admin-password 'Fiap@2tdsvms' \
   --enable-public-network true
   @@@

3. Criar o Banco de Dados (PaaS)
   @@@bash
   az sql db create \
   --resource-group rg-medix-rm559728 \
   --server sql-server-medix-rm559728 \
   --name db-medix \
   --service-objective Basic \
   --backup-storage-redundancy Local \
   --zone-redundant false
   @@@

4. Liberar Regras de Firewall do Banco de Dados
   @@@bash
   az sql server firewall-rule create \
   --resource-group rg-medix-rm559728 \
   --server sql-server-medix-rm559728 \
   --name liberaGeral \
   --start-ip-address 0.0.0.0 \
   --end-ip-address 255.255.255.255
   @@@

5. Criar o Registro de Contêiner da Azure (Azure Container Registry - ACR)
   @@@bash
   az acr create --resource-group rg-medix-rm559728 --name acrmedixrm559728 --sku Basic --admin-enabled true
   @@@

---

## Scripts de Estrutura do Banco de Dados (DDL)

As tabelas devem ser criadas com iniciais em letra maiúscula diretamente no **Query Editor** do banco de dados no portal da Azure para manter compatibilidade com o mapeamento das entidades do JPA Hibernate:

@@@sql
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
@@@

---

## Instruções de Carga e Massa de Testes (DML)

Execute o comando abaixo no Query Editor para popular o banco de dados com a massa de testes contendo as informações dos integrantes do grupo:

@@@sql
-- Inserção da massa de dados inicial
INSERT INTO Colaboradores (nome, cargo, setor) VALUES
('Mateus da Silveira Lima', 'Desenvolvedor Java', 'TI - Desenvolvimento'),
('Arthur Thomas Mariano de Souza', 'Desenvolvedor C#', 'TI - Desenvolvimento'),
('Davi Cavalcanti Jorge', 'Desenvolvedor Mobile', 'TI - Desenvolvimento');

-- Validação da carga efetuada
SELECT * FROM Colaboradores;
@@@

---

## Configuração das Conexões no Azure DevOps

Antes de rodar a pipeline, é obrigatório criar as credenciais de autenticação dentro do painel do Azure DevOps para que ele possa gerenciar os recursos criados na Azure e no GitHub.

### 1. Criar a Conexão com o Azure Container Registry (Conexao-ACR)
1. No painel lateral esquerdo do Azure DevOps, clique em **Project Settings** (ícone de engrenagem).
2. Vá em **Service connections** sob a seção *Pipelines* e clique em **New service connection**.
3. Selecione a opção **Docker Registry** e clique em *Next*.
4. Altere o campo *Registry type* de Azure Container Registry para **Others**.
5. Preencha os campos com os dados administrativos obtidos na aba *Access Keys* do seu ACR no portal da Azure:
    - **Docker Registry:** `https://acrmedixrm559728.azurecr.io`
    - **Docker ID:** `acrmedixrm559728`
    - **Docker Password:** *(Cole a chave de acesso gerada pelo portal da Azure)*
6. Defina o nome exato da conexão como: `Conexao-ACR`
7. Marque a opção **"Grant access permission to all pipelines"** e clique em **Save**.

### 2. Criar a Conexão com a Assinatura Azure (Conexao-Azure-DevOps)
1. Na tela de **Service connections**, clique em **New service connection**.
2. Escolha **Azure Resource Manager** e avance.
3. Escolha o método **Service principal (automatic)** e avance.
4. Faça o login com a sua conta estudantil caso seja solicitado.
5. Preencha os campos de escopo:
    - **Subscription:** Selecione a sua assinatura ativa.
    - **Resource group:** Selecione o grupo `rg-medix-rm559728`.
6. Configure o nome exato da conexão como: `Conexao-Azure-DevOps`
7. Ative a caixinha **"Grant access permission to all pipelines"** e clique em **Save**.

### 3. Vincular o Repositório do GitHub nas Pipelines
1. No menu principal do Azure DevOps, acesse a aba **Pipelines** e clique em **New pipeline** (ou *Create Pipeline*).
2. Na tela de seleção de origem, selecione a opção **GitHub**.
3. Você será redirecionado para autorizar o acesso à sua conta. Conceda a permissão e selecione o repositório do projeto: `challengeoracle/sprint-03-devops`.
4. Na tela de configuração, escolha a opção **Existing Azure Pipelines YAML file**.
5. Indique a branch `main` e aponte o caminho para o arquivo `/azure-pipelines.yml` contido na raiz do projeto. Clique em continuar e salve a configuração.

---

## Esteira Automatizada de DevOps

### 1. Configuração do Web App Nativo para Docker
Execute estes comandos no Azure Cloud Shell para provisionar o Web App preparado para rodar contêineres e estender o tempo de limite de inicialização do Spring Boot:

@@@bash
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
@@@

### 2. Configuração do Dockerfile
Crie um arquivo chamado `Dockerfile` na raiz do projeto com as seguintes instruções:

@@@dockerfile
FROM eclipse-temurin:21-jdk-alpine
EXPOSE 8080
ARG JAR_FILE=target/medixchamados-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
@@@

### 3. Arquivo de Pipeline Azure DevOps
Este é o arquivo `azure-pipelines.yml` que deve ficar localizado na raiz do seu código do GitHub para automatizar os estágios de Build (CI) e Deploy (CD):

@@@yaml
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
          @@@

---

## Deploy Manual da Aplicação (Sem Pipeline - Legado)

### Configuração do Web App Legado
Configuração para subida manual do arquivo bruto `.jar` sem utilizar contêineres Docker:

@@@bash
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
@@@

### Execução do Deploy Manual

1. Execute a geração do pacote `.jar` localmente:
   @@@bash
   mvnw clean package -DskipTests
   @@@

2. Faça o upload do arquivo binário gerado na pasta `target/` para a Azure através do terminal do console.

3. Inicie o deploy manual via Azure Cloud Shell:
   @@@bash
   az webapp deploy \
   --resource-group rg-medix-rm559728 \
   --name web-medix-rm559728 \
   --src-path medixchamados-0.0.1-SNAPSHOT.jar \
   --type jar
   @@@

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


