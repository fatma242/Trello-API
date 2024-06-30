package service;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import messagingSystem.Client;
import model.Card;
import model.ListEntity;
import model.User;

@Stateless
public class ListService {
	
	@PersistenceContext(unitName = "hello")
	EntityManager entityManager;
	@Inject
	Client messageClient;
	public Response createList(@PathParam("userId") Long userId, ListEntity listEntity) {
		User u = entityManager.find(User.class, userId);
		if (u == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("TeamLeader not found.").build();
		}
		if (u.getRole().equals("Collaborator")) {
			return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
		}
		for (Card card : listEntity.getCards()) {
            card.setList(listEntity);  
        }
        try {
            entityManager.persist(listEntity);
            messageClient.sendMessage("List created: " + listEntity.getName());
            entityManager.flush();
            return Response.status(Response.Status.OK).entity(listEntity).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
	}
	
	public Response deleteList(@PathParam("listId") Long listId, @PathParam("userId") Long userId) {
		User u = entityManager.find(User.class, userId);
		ListEntity listEntity = entityManager.find(ListEntity.class, listId);
		if (u == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("TeamLeader not found.").build();
		}
		if (u.getRole().equals("Collaborator")) {
			return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
		}
		if (listEntity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("List not found.").build();
        }
		try {
            entityManager.remove(listEntity);
            messageClient.sendMessage("List deleted: " + listEntity.getName());
            entityManager.flush();
            return Response.status(Response.Status.OK).entity("List deleted successfully.").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
	}
}
