package ch.hearc.ig.guideresto.business;

/**
 * @author cedric.baudet
 */
public class EvaluationCriteria implements IBusinessObject {

    private Integer id;
    private String name;
    private String description;

    @SuppressWarnings("unused")
    public EvaluationCriteria() {
        this(null, null);
    }

    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unused")
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @SuppressWarnings("unused")
    public void setDescription(String description) {
        this.description = description;
    }
}