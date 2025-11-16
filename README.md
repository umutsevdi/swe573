# SWE-573 SOFTWARE DEVELOPMENT PRACTICE

ID: 2024719168 Umut Can Sevdi

## Installation

- Use the docker-compose.yaml provided in the repository.
- Update the credentials provided in the `env` file.


### Environment Variables

 - `PUBLIC_API` Path to back-end server.
 - `SERVER_API` (Optional) The base URL used internally by the front-end server 
to communicate with back-end. This can be set to an internal address
(e.g. http://containerName:Port). By default it's set to `PUBLIC_API`.
