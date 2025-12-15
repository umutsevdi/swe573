# SWE-573 SOFTWARE DEVELOPMENT PRACTICE

ID: 2024719168 Umut Can Sevdi

## Installation

- Use the docker-compose.yaml provided in the repository.
- Update the credentials provided in the `env` file.

### Installing With Pre-Built Images
Images for the Hive are available at [hub.docker.com](https://hub.docker.com).
Running `docker-compose up` will automatically pull them from the image
repositories.

### Building From Source

#### Hive Back-end
1. Install Java requirements:
```sh
sudo apt install openjdk-17-jdk-headless maven -y
```

2. Create the environment variables
```sh
cp prod.env backend/Hive/test.env
```
- Run the bash script.
```sh
cd backend/Hive
bash buildimage.sh
```

#### Hive-Fe
```sh
sudo apt install npm nodejs -y
```

2. Run the following command
```sh
npm update && npm install
PUBLIC_API=https://localhost:8080 npm run build
PUBLIC_API=https://localhost:8080 docker build . -t sevdiumut/hive-fe
```


### Environment Variables

#### Required Arguments
- `PUBLIC_API` Path to back-end server.
- `HIVE_PASSWORD` Password for the root account.
- `POSTGRES_HOST` Postgresql database hostname.
- `POSTGRES_USER` Postgresql user
- `POSTGRES_PASSWORD` Postgresql password
- `JWT_SECRET` JWT signing key.
- `ALLOWED_ORIGINS`: CORS configuration. 
> [!NOTE]
> ALLOWED_ORIGINS environment variable is required for both front-end 
> and back-end. However, the syntax for the wildcard characters for back-end
> and front-end are different due to language differences. Use recommended
> options non-production cases.

#### Optional Arguments
 - `HIVE_USERNAME` Username for the root account.
 - `SERVER_API` (Optional) The base URL used internally by the front-end server 
to communicate with back-end. This can be set to an internal address
- `JWT_ISSUER` JWT issuer.
(e.g. http://containerName:Port). By default it's set to `PUBLIC_API`.
- `spring_profiles_active` Enables/disables swagger-ui.
