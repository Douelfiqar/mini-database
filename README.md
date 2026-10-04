# miniDB

I'm building this to learn how a database turns a SQL command into data on disk. It's a small Java server with my own tokenizer, parser, and file storage. It only understands a few commands so far.

## Run it

Open the project in IntelliJ with JDK 24 and run `com.idriss.tiktokjunior.server.Main`. If you have Maven, you can also run it from a terminal in the project folder:

```bash
mvn compile
java -cp target/classes com.idriss.tiktokjunior.server.Main
```

The server listens on port `6389`. Connect with a TCP client (`nc localhost 6389` on macOS/Linux, or `ncat localhost 6389` on Windows if you have Ncat) and send SQL commands ending in `;`. You can put several commands on one line or spread one command over multiple lines.

## Try it

```sql
CREATE TABLE users (id LONG PRIMARY KEY, name VARCHAR(50));
INSERT INTO users VALUES (1, 'Ada');
SELECT * FROM users;

BEGIN;
INSERT INTO users VALUES (2, 'Grace');
ROLLBACK;
SELECT name FROM users;
```

`COMMIT;` writes the statements queued after `BEGIN;`. `ROLLBACK;` throws them away. Without `BEGIN;`, a command runs right away. The server sends one response line per command.

Table definitions go in `data/<table>.tbl`, and rows go in `data/<table>.data`. The `data` folder is created wherever you start the server.

## Current limits

- Supported commands are `CREATE TABLE`, `INSERT INTO ... VALUES`, `SELECT ... FROM`, `BEGIN`, `COMMIT`, and `ROLLBACK`.
- `SELECT` inside a transaction is not supported yet. There is no `WHERE`, `UPDATE`, or `DELETE`.
- A bad statement is checked before a transaction starts writing, but a disk error or crash during `COMMIT` can still leave partial changes.
- Column types, `VARCHAR` lengths, and primary key uniqueness are recorded but not enforced yet.

I'm still treating it as an experiment, so I wouldn't put important data in it yet.
