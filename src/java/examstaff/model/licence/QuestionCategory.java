package examstaff.model.licence;

public class QuestionCategory {

    private int questionCategoryId;
    private String categoryName;
    private String description;

    public QuestionCategory() {
    }

    public QuestionCategory(int questionCategoryId, String categoryName, String description) {
        this.questionCategoryId = questionCategoryId;
        this.categoryName = categoryName;
        this.description = description;
    }

    public int getQuestionCategoryId() {
        return questionCategoryId;
    }

    public void setQuestionCategoryId(int questionCategoryId) {
        this.questionCategoryId = questionCategoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
