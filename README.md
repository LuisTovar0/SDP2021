# SDP2021 File Distribution System

A distributed file hosting system built in Java implementing the custom SDP2021 (Service Desk Protocol 2021) communication protocol. This project creates a network of file distribution centers and hosting nodes that work together to store and manage files across multiple remote locations.

## Overview

The system consists of three main components:
- **Distribution Center**: Central hub that receives files from clients and distributes them to available hosting nodes
- **Hosting Nodes (Alojadores)**: Remote servers that store files and report their availability 
- **Client Emitters**: Applications that send files to the distribution center

All communication between components uses the custom SDP2021 protocol over TCP connections on port 32507.

## SDP2021 Protocol

The Service Desk Protocol 2021 is a custom TCP-based protocol designed for reliable data transactions. Each message follows this structure:

| Field | Position | Length | Description |
|-------|----------|---------|-------------|
| Version | 0 | 1 byte | Protocol version (0 for initial version) |
| Code | 1 | 1 byte | Message type identifier (0-255) |
| Data Length | 2 | 1 byte | Number of bytes in data field (0-255) |
| Data | 3 | Variable | Actual message payload |

### Message Types

- **0**: Test - Connection test with no side effects
- **1**: End - Close connection request
- **2**: Acknowledged - Success response
- **3**: End of file - Marks completion of file transfer
- **4**: File name - Specifies destination filename
- **5**: Error - Indicates operation failure
- **6**: Available - Hosting node availability notification
- **255**: Segment - File data segment (for files >255 bytes)

## Architecture

### Distribution Center (`CentroDistribuicao`)
- Maintains a list of available hosting nodes
- Monitors local folder for incoming files from clients
- Distributes files to hosting nodes using round-robin scheduling
- Provides HTTP dashboard for system monitoring (optional)
- Handles hosting node availability management

### Hosting Nodes (`Alojador`)
- Register availability with distribution center on startup
- Receive and store files from distribution center
- Support concurrent file transfers
- Store files in local directories

### Client Emitters (`ClienteEmissor`)
- Send files to distribution center using SDP2021 protocol
- Support large file transfers through segmentation
- Handle connection management and error recovery

## Project Structure

```
src/
├── Alojador/
│   └── Alojador.java              # Hosting node implementation
├── CentroDistribuicao/
│   ├── CentroDistribuicao.java    # Distribution center main logic
│   └── AlojadoresQueue.java       # Hosting node queue management
├── sdp2021/
│   ├── SDP.java                   # Protocol interface and constants
│   ├── Client.java                # SDP client implementation
│   ├── Server.java                # SDP server implementation
│   └── Mensagem.java              # Message parsing utilities
├── AlojadorMain.java              # Hosting node entry point
├── CentroDistribuicaoMain.java    # Distribution center entry point
├── ClienteEmissorMain.java        # Client emitter entry point
└── ServerMain.java                # Generic server entry point
```

## Building and Running

### Prerequisites
- Java 8 or higher
- Apache Ant build system

### Build
```bash
make server    # Build and run generic server
make client    # Build and run client emitter
make alojador  # Build and run hosting node
make distribuicao  # Build and run distribution center
```

Or manually with Ant:
```bash
ant
cd out
java [MainClass]
```

### Running the System

1. **Start Distribution Center**:
   ```bash
   make distribuicao
   ```

2. **Start Hosting Nodes**:
   ```bash
   make alojador
   # Enter distribution center IP when prompted
   # Enter hosting node IP for registration
   ```

3. **Send Files**:
   ```bash
   make client
   # Enter distribution center IP when prompted
   ```

## Features

- **Fault Tolerance**: Automatic handling of unavailable hosting nodes
- **Load Balancing**: Round-robin distribution across available nodes
- **File Segmentation**: Support for files larger than 255 bytes
- **Monitoring**: Real-time system status tracking
- **Scalability**: Easy addition of new hosting nodes

## Configuration

- **Port**: TCP port 32507 (defined in `Server.port`)
- **File Storage**: 
  - Distribution center: `sdp2021/resources/serverStorage/`
  - Hosting nodes: Local storage directories
- **Node Registry**: `sdp2021/resources/serverStorage/infos/alojadores.txt`

## Sample Files

The project includes sample files from "Os Lusíadas" by Luís Vaz de Camões:
- `cabeçalho.txt` - Header/introduction
- `canto1.txt` - First canto
- `canto2.txt` - Second canto

## Network Communication

The system uses TCP sockets for all inter-component communication:
- Client-to-server connections initiated by clients
- Persistent connections maintained during file transfers
- Graceful connection termination with proper cleanup
- Error handling and recovery mechanisms

## Development Notes

This project was developed as part of Sprint 4-5 for the RCOMP (computer networks) 2020-2021 subject of the ISEP (Instituto Superior de Engenharia do Porto) Informatics Engineering Bachelor's degree course. It demonstrates network programming concepts including:
- Custom protocol design and implementation
- Multi-threaded server architecture
- Distributed system coordination
- File transfer over networks
- Error handling in network applications

## Future Enhancements (Sprint 5)

Planned improvements include:
- HTTPS support for web dashboard
- SSL/TLS encryption with mutual certificate authentication
- Enhanced security features
- Improved monitoring capabilities
