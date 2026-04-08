# Checkpoint 05 - Medix (Sistema de Tickets para Unidades de Saúde)

Este projeto apresenta um microserviço desenvolvido em Java Spring Boot para a gestão de chamados de suporte técnico (tickets) em unidades de saúde (clínicas e hospitais). A solução é integrada ao Azure SQL Database e permite que colaboradores registrem falhas técnicas em equipamentos ou sistemas, com monitoramento de prioridade e status.

---

## Menu de Navegação

- [Checkpoint 05 - Medix (Sistema de Tickets para Unidades de Saúde)](#checkpoint-05---medix-sistema-de-tickets-para-unidades-de-saúde)
    - [Menu de Navegação](#menu-de-navegação)
    - [Configuração do Banco de Dados (Azure SQL)](#configuração-do-banco-de-dados-azure-sql)
        - [Executar no Azure Cloud Shell](#executar-no-azure-cloud-shell)
    - [Scripts de Estrutura (DDL)](#scripts-de-estrutura-ddl)
        - [Executar no Azure PowerShell](#executar-no-azure-powershell)
    - [Configuração do Web App (API Java)](#configuração-do-web-app-api-java)
        - [Executar no Azure Cloud Shell](#executar-no-azure-cloud-shell-1)
    - [Deploy da Aplicação](#deploy-da-aplicação)
        - [Executar na sua Máquina Local (Raiz do Projeto)](#executar-na-sua-máquina-local-raiz-do-projeto)
        - [Executar no Azure Cloud Shell](#executar-no-azure-cloud-shell-2)
    - [Documentação da API (Endpoints)](#documentação-da-api-endpoints)
        - [Exemplos de Requisição (JSON)](#exemplos-de-requisição-json)
    - [Vídeo Demonstrativo](#vídeo-demonstrativo)
    - [Integrantes do Grupo](#integrantes-do-grupo)

---

## Configuração do Banco de Dados (Azure SQL)

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

4. Liberar Regras de Firewall
    ```bash
    az sql server firewall-rule create \
    --resource-group rg-medix-rm559728 \
    --server sql-server-medix-rm559728 \
    --name liberaGeral \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 255.255.255.255
    ```

---

## Scripts de Estrutura (DDL)

### Executar no Azure PowerShell

5. Criação das Tabelas (Relacionamento Colaborador -> Chamados)

```bash
Invoke-Sqlcmd -ServerInstance "sql-server-medix-rm559728.database.windows.net" `
    -Database "db-medix" `
    -Username "user-medix" `
    -Password "Fiap@2tdsvms" `
    -Query @"
        -- Tabela de Colaboradores
        CREATE TABLE colaboradores (
        id_colaborador INT IDENTITY(1,1) PRIMARY KEY,
        nome VARCHAR(100) NOT NULL,
        cargo VARCHAR(50),
        setor VARCHAR(50)
        );


         -- Tabela de Chamados de Suporte
         CREATE TABLE chamados (
         id_chamado INT IDENTITY(1,1) PRIMARY KEY,
         id_colaborador INT NOT NULL,
         descricao VARCHAR(MAX) NOT NULL,
         prioridade VARCHAR(20),
         status VARCHAR(20) DEFAULT 'ABERTO',
         data_abertura DATETIME DEFAULT GETDATE(),
         CONSTRAINT FK_Colaborador_Chamado FOREIGN KEY (id_colaborador) REFERENCES colaboradores(id_colaborador)
         );
     "@
```

---

## Configuração do Web App (API Java)

### Executar no Azure Cloud Shell

1. Criar o Plano de Serviço

    ```bash
    az appservice plan create --name plan-medix-rm559728 --resource-group rg-medix-rm559728 --location southafricanorth --sku F1 --is-linux
    ```

2. Criar o Web App

    ```bash
    az webapp create \
    --name web-medix-rm559728 \
    --resource-group rg-medix-rm559728 \
    --plan plan-medix-rm559728 \
    --runtime "JAVA|21-java21"
    ```

3. Configurar Variáveis de Ambiente
```bash
    az webapp config appsettings set --name web-medix-rm559728 --resource-group rg-medix-rm559728 --settings \
    SPRING_DATASOURCE_URL="jdbc:sqlserver://sql-server-medix-rm559728.database.windows.net:1433;database=db-medix;encrypt=true;trustServerCertificate=false;" \
    SPRING_DATASOURCE_USERNAME="user-medix" \
    SPRING_DATASOURCE_PASSWORD="Fiap@2tdsvms"
```

---

## Deploy da Aplicação

### Executar na sua Máquina Local (Raiz do Projeto)

1. Gerar o artefato `.jar`
```bash
    mvnw clean package -DskipTests
  ```

### Executar no Azure Cloud Shell

2. Fazer upload do arquivo `.jar` para o Azure através do console.

3. Iniciar o Deploy do arquivo `.jar`
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

## Vídeo Demonstrativo

Demonstração das funcionalidades, interface Thymeleaf e persistência dos dados no Azure SQL:

- [Link para o Vídeo no YouTube](INSERIR A URL DO V[IDEO AQUI])

---

## Integrantes do Grupo

- RM561061 - Arthur Thomas Mariano de Souza
- RM559873 - Davi Cavalcanti Jorge
- RM559728 - Mateus da Silveira Lima
