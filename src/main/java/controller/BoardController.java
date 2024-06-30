package controller;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import service.BoardService;

@RolesAllowed("TeamLeader")
@Path("/boards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BoardController {

    @EJB
    private BoardService boardService;

    @POST
    @Path("/create/{userId}/{name}")
    public Response createBoard(@PathParam("userId") Long userId, @PathParam("name")String name) {
        return boardService.createBoard(userId, name);
    }

    @GET
    @Path("/getAllBoardByTeamLeader/{teamLeaderId}")
    public Response getAllBoardsByTeamLeader(@PathParam("teamLeaderId") Long teamLeaderId) {
        return boardService.getAllBoardsByTeamLeader(teamLeaderId);
    }

    @PUT
    @Path("/invite/{boardId}/{inviterId}/{teamLeaderId}")
    public Response inviteCollaborator(@PathParam("boardId") Long boardId, @PathParam("inviterId") Long inviterId, @PathParam("teamLeaderId") Long teamLeaderId) {
        return boardService.inviteCollaborator(boardId, inviterId, teamLeaderId);
    }

    @DELETE
    @Path("/delete/{boardId}/{userId}")
    public Response deleteBoard(@PathParam("boardId") Long boardId, @PathParam("userId") Long userId) {
        return boardService.deleteBoard(boardId, userId);
    }
}
