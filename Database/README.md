# Quick Guide: Install Docker, Docker Compose, and Set Up a Local MySQL Database

## 1. Install Docker Desktop

- Go to [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)
- Download Docker Desktop for Windows and run the installer.
- Follow the installation instructions and restart your computer if prompted.
- After installation, launch Docker Desktop and ensure it is running.

## 2. Verify Docker Installation

Open a terminal (Command Prompt or PowerShell) and run:
```sh
docker --version
docker compose version
```
You should see version information for both Docker and Docker Compose.

## 3. Create a Docker Compose File and environment file
### 3.1 Create a Docker Compose File if not already existing
Create a new folder for your project and inside it, create a file named `docker-compose.yml` with the following content:

```yaml
version: '3.8'
services:
  mysql:
    image: mysql:8.0
    container_name: local-startspeler-mysql
    env_file:
      - .env
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql

volumes:
  mysql_data:
```
### 3.2 Create a environement file if not already existing
Create a new ```.env``` file including the following code:

```yaml
# Do NOT commit this file to git. Add it to .gitignore.
MYSQL_ROOT_PASSWORD=change_this_root_password
MYSQL_DATABASE=startspelerdb
MYSQL_USER=startspeler
MYSQL_PASSWORD=change_this_user_password
```

The passwords are shared in a secure manner with the team. Replace the examples with the shared passwords

## 4. Start the MySQL Database

Go to the project folder, starting from the project folder:

```sh
cd .\Database\docker\
```

In your docker folder, open a terminal and run:

```sh
docker compose up -d
```

This will download the MySQL image (if needed) and start the database in the background based on the docker compose file.

## 5. Connect to MySQL

You can now connect to your local MySQL database using a client (like MySQL Workbench) with:

- **Host:** `localhost`
- **Port:** `3306`
- **User:** `exampleuser`
- **Password:** `examplepass`
- **Database:** `exampledb`

## 6. Stop and Remove the Database

To stop the database, run:

```sh
docker compose down
```

This will stop and remove the MySQL container, but your data will persist in the `mysql_data` volume.