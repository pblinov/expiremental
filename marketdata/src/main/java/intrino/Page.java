package intrino;

import java.util.List;

/**
 * @author pblinov
 * @since 15/09/2017
 */
public class Page extends Security {
    public List<Security> data;
    public Integer result_count;
    public Integer page_size;
    public Integer current_page;
    public Integer total_pages;
    public Integer api_call_credits;
}
