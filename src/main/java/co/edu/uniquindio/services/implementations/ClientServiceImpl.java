package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.model.Client;
import co.edu.uniquindio.services.interfaces.ClientService;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

    @Override
    public String processClient(Client client) {

        return "Client processed: " + client.getName() + ", Email: " + client.getEmail();
    }
}

