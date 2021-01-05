package application.database.repositories;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface MessageRepository extends CrudRepository<Message, Integer> {
    public List<Message> findAllByChatOrderByMessageId(Chat chat);
}
