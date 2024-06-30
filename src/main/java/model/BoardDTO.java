package model;

import java.util.List;
import java.util.stream.Collectors;

public class BoardDTO {
	
	private long id;
	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
	private String name;
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	private List<String> collaborators;
	public void setCollaborators(List<String> collaborators) {
		this.collaborators = collaborators;
	}
	public List<String> getCollaborators() {
		return collaborators;
	}
	public BoardDTO(Board board) {
		this.id = board.getId();
		this.name = board.getName();
		this.collaborators = board.getCollaborators().stream().map(User::getName).collect(Collectors.toList());
	}
}
