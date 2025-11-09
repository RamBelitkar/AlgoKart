# AlgoKart
This repo is basically for the completion of the assignment given by the algokart

# Assigment
Build a simple TCP chat server (no HTTP, no database) that allows multiple users to connect, log in with a
username, and chat with each other in real time.
You can use any programming language you’re comfortable with (Python, Node.js, Go, Java, etc.).
Use only the standard library for sockets — no frameworks or external chat libraries.

## Abstract
To ensure efficient communication between multiple clients without deadlocks, **multithreading** is used.  
Each client is handled by a separate thread (`ClientHandler`), allowing simultaneous read/write operations on different channels. 
The project includes:
- A **Client** that connects to the server and performs secure login using hashed credentials.
- A **Server** that listens on port 4000 for new client connections.
- A **ClientHandler** that manages user authentication, message broadcasting, and session management.


## Methodology
The project consists of **three main classes**:

### 1. `Server.java`
- Acts as the **main server socket**.
- Listens on port **4000**.
- Accepts client connections and creates a new `ClientHandler` thread for each client.

### 2. `ClientHandler.java`
- Handles individual client sessions.
- Reads client credentials (`LOGIN` and `PASS`).
- Validates user authentication.
- Broadcasts messages to all connected clients.
- Removes clients who disconnect from the server.

### 3. `Client.java`
- Acts as the **user-side application**.
- Connects to the server using the specified IP and port.
- Sends login credentials.
- Allows the user to send and receive messages in real time.
- Can execute commands like:
  - `WHO` → Lists active users.
  - `exit` → Disconnects from the server.

> By default, the client connects to `localhost:4000`,  
> but this can be changed to a public IP to enable remote access.

## How to Run
```bash
javac Server.java ClientHandler.java Client.java
```

### To Start the Server
```bash
java Server
```

### To Start the Client
```bash
java Client
```


