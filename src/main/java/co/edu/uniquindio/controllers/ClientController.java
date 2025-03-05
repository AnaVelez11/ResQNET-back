package co.edu.uniquindio.controllers;

import co.edu.uniquindio.model.Client;
import co.edu.uniquindio.services.interfaces.ClientService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    public String receiveClient(@RequestBody Client client) {
        return "Client received: " + client.getName() + ", Email: " + client.getEmail();
    }
}

