package service;

import java.util.List;
import java.util.stream.Collectors;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;

import messagingSystem.Client;
import model.Board;
import model.BoardDTO;
import model.User;

@Stateless
public class BoardService {

	@PersistenceContext(unitName = "hello")
	EntityManager entityManager;
	@Inject
	Client messageClient;
	public Response createBoard(Long userId, String name) {
		long count = entityManager.createQuery("SELECT COUNT(b) FROM Board b WHERE b.name = :name", Long.class)
                .setParameter("name", name)
                .getSingleResult();
		User u = entityManager.find(User.class, userId);
		if (u == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("TeamLeader not found.").build();
		}
		if (u.getRole().equals("Collaborator")) {
			return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
		}
		if (count > 0) {
			return Response.status(Response.Status.CONFLICT).entity("A board with the same name already exists!").build();
		}
		try {
			Board board = new Board();
			board.setName(name);
			board.setTeamLeader(u);
			entityManager.persist(board);
			messageClient.sendMessage("Board created: " + board.getName());
			entityManager.flush();
			return Response.status(Response.Status.OK).entity(board).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	
	public Response getAllBoardsByTeamLeader(Long teamLeaderId) {
		User user = entityManager.find(User.class, teamLeaderId);
		if (user == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("TeamLeader not found.").build();
		}
		if (user.getRole().equals("Collaborator")) {
			return Response.status(Response.Status.FORBIDDEN).entity("Forbidden")
					.build();
		}
		try {
			List<Board> boards = entityManager.createQuery(
	                "SELECT b FROM Board b JOIN FETCH b.teamLeader tl LEFT JOIN FETCH b.collaborators WHERE tl.id = :teamLeaderId", 
	                Board.class)
	                .setParameter("teamLeaderId", teamLeaderId)
	                .getResultList();
			messageClient.sendMessage("Boards create by " +user.getName()+" are returned");
	
	        if (boards.isEmpty()) {
	            return Response.status(Response.Status.NOT_FOUND).entity("No boards found for the team leader.").build();
	        }
	        return Response.status(Response.Status.OK).entity(boards.stream().map(BoardDTO::new).collect(Collectors.toList())).build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

	public Response inviteCollaborator(Long boardId, Long userId, Long teamLeaderId) {
		Board board = entityManager.find(Board.class, boardId);
		User collaborator = entityManager.find(User.class, userId);
		User user = entityManager.find(User.class, teamLeaderId);
		if (board == null || collaborator == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("Board or user not found.").build();
		}
		if (user.getRole().equals("Collaborator")) {
			return Response.status(Response.Status.FORBIDDEN).entity("Forbidden")
					.build();
		}
		board.getCollaborators().add(collaborator);
		entityManager.merge(board);
		messageClient.sendMessage("Collaborator added: " + collaborator.getName());
		entityManager.flush();
		return Response.status(Response.Status.OK).entity("Collaborator invited successfully.").build();
	}

	
	public Response deleteBoard(Long boardId,  Long userId) {
		Board board = entityManager.find(Board.class, boardId);
		User u = entityManager.find(User.class, userId);
		if (u == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("TeamLeader not found.").build();
		}
		if (u.getRole().equals("Collaborator")) {
			return Response.status(Response.Status.FORBIDDEN).entity("Forbidden").build();
		}
		if (board == null) {
			return Response.status(Response.Status.NOT_FOUND).entity("Board not found.").build();
		}
		try {
			entityManager.remove(board);
			messageClient.sendMessage("Board " + board.getName()+" is deleted successfully." );
			entityManager.flush();
			return Response.status(Response.Status.OK).entity("Board deleted successfully.").build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
	}

}
