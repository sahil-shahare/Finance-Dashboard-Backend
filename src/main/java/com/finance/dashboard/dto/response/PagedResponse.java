package com.finance.dashboard.dto.response;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
public class PagedResponse<T> {
    private List<T> content; private int page,size,totalPages; private long totalElements; private boolean last;
    private PagedResponse(Builder<T> b){this.content=b.content;this.page=b.page;this.size=b.size;this.totalElements=b.totalElements;this.totalPages=b.totalPages;this.last=b.last;}
    public static <T> Builder<T> builder(){return new Builder<>();}
    public static class Builder<T> {
        private List<T> content; private int page,size,totalPages; private long totalElements; private boolean last;
        public Builder<T> content(List<T> v){this.content=v;return this;} public Builder<T> page(int v){this.page=v;return this;}
        public Builder<T> size(int v){this.size=v;return this;} public Builder<T> totalElements(long v){this.totalElements=v;return this;}
        public Builder<T> totalPages(int v){this.totalPages=v;return this;} public Builder<T> last(boolean v){this.last=v;return this;}
        public PagedResponse<T> build(){return new PagedResponse<>(this);}
    }
    public static <S,T> PagedResponse<T> from(Page<S> page, Function<S,T> mapper){
        return PagedResponse.<T>builder().content(page.getContent().stream().map(mapper).collect(Collectors.toList())).page(page.getNumber()).size(page.getSize()).totalElements(page.getTotalElements()).totalPages(page.getTotalPages()).last(page.isLast()).build();
    }
    public List<T> getContent(){return content;} public int getPage(){return page;} public int getSize(){return size;}
    public long getTotalElements(){return totalElements;} public int getTotalPages(){return totalPages;} public boolean isLast(){return last;}
}
