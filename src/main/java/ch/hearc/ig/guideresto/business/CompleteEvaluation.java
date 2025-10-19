package ch.hearc.ig.guideresto.business;



import java.util.Date;
import java.util.HashSet;
import java.util.Set;
/**
 * @author cedric.baudet
 */
public class CompleteEvaluation extends Evaluation {

    private String comment;
    private String username;
    private Set<Grade> grades;

    @SuppressWarnings("unused")
    public CompleteEvaluation() {
        this(null, null, null, null);
    }

    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
        this.grades = new HashSet<>();
    }

    public String getComment() {
        return comment;
    }

    @SuppressWarnings("unused")
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}