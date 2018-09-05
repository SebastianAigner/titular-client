package index

enum class GamePhase(val desc: String) {
    NEED_NAME("Need name."),
    NEED_GAME_ID("Need Game Id."),
    WAITING_FOR_NEXT_ROUND("Waiting for next round"),
    GUESS("Enter your guess"),
    VOTE("Vote for the best results"),
    WAITING_FOR_FIRST_GAME("Waiting for first game")
}