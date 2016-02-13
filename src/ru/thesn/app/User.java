package ru.thesn.app;


public class User implements Comparable<User> {
    private String name;
    private int messages;
    private int battles;
    private int percent;
    private boolean isAdmin;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMessages() {
        return messages;
    }

    public void setMessages(int messages) {
        this.messages = messages;
    }

    public int getBattles() {
        return battles;
    }

    public void setBattles(int battles) {
        this.battles = battles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return name.equals(user.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    @Override
    public int compareTo(User o) {
        return o.getMessages() - messages;
    }

    @Override
    public String toString() {
        String percent = getPercent() == 0 ? "" : "(" + String.valueOf(getPercent() + "%)");
        return String.format("%s      %s     %s %s", format(name, 25), format(messages, 6), format(battles >= 0 ? battles : "?", 6), percent);
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public <T> String format(T t, int count){
        StringBuilder sb = new StringBuilder(t + "");
        while (sb.length() < count)
            sb.append(" ");
        return sb.toString();
    }
}
