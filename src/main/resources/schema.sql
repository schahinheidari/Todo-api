CREATE TABLE user (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(255) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            roles VARCHAR(255),
                            FOREIGN KEY (user_id) REFERENCES user(id)
);

CREATE TABLE task (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      title VARCHAR(255),
                      description VARCHAR(255),
                      done BOOLEAN,
                      owner_id BIGINT,
                      FOREIGN KEY (owner_id) REFERENCES user(id)
);