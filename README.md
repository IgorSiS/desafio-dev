-# DESAFIO

### Tecnologias utilizadas backend
- Java 17
- Spring Boot 3.1.1
- Docker
- MySql
- Open API
- Lombok
- MapStruct
- Spring Test
### Tecnologias utilizadas frontend
- Não tenho muito conhecimento em front,mas sei um pouco de HTML, CSS, Java script puro.

Sugestão de SDK: No intelij baixar o amazon correto-17. Amazon Correto version 17.0.5


Executando a aplicação local

### Subindo banco de dados

- Abra um terminal na pasta raiz do projeto(onde tem o pom.xml) e execute o banco de dados:  `docker-compose up -d mysql`
- Execute a aplicação através do IntelliJ OU certifique-se de que o Maven esteja instalado no seu sistema caso não queira executar pelo intelij.
  Navegue até o diretório raiz do seu projeto onde está localizado o arquivo pom.xml e execute o comando: `mvn spring-boot:run`.
- Com a aplicação de pé acesse: `http://localhost:8080/swagger-ui/index.html`. Poderá ver todas as funcionalidades.

### FrontEned
- Para usar o frontend, apenas abra o arquivo index.html que foi enviado junto ao projeto, no seu navegador

### Testando as funcionalides:
- Fiz testes apenas das regras de negócio - CAMADA DE CASO DE USO.
- Acessar swagger ou importe no postman o curl das request que irei deixar logo abaixo:
- Observações: Para testar o recurso de enviar o arquivo é preferível pelo postman, pois o swagger tem limitação com relação a envio de arquivo.
```
Extraindo transações do arquivo
- Para anexar o arquivo no postman, importe esse request, vá no aba de body e depois form-data.
  No campo key será: file e value: /arquivo da sua máquina

curl --location 'http://localhost:8080/transaction/process/data' \
--form 'file=@"/C:/Users/anton/CNAB.txt"'
---------------------------------------------------------------

Consultando transações - Vai retornar uma consulta paginada

curl --location 'http://localhost:8080/transaction?storeName=BAR%20DO%20JO%C3%83O' \
--header 'accept: */*'
---------------------------------------------------------------
```
