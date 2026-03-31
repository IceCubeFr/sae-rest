package dto;

public record User(int id, String login, String password, Role role) {
}
