package ltphat.inventory.backend.catalog.infrastructure.persistence;

import ltphat.inventory.backend.catalog.domain.model.Category;
import ltphat.inventory.backend.catalog.infrastructure.persistence.entity.JpaCategory;
import ltphat.inventory.backend.catalog.infrastructure.persistence.mapper.CategoryMapper;
import ltphat.inventory.backend.catalog.infrastructure.persistence.repository.SpringDataCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryRepositoryImplTest {

    @Mock
    private SpringDataCategoryRepository springDataCategoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryRepositoryAdapter repository;

    @Test
    void save_shouldMapAndPersist() {
        Category category = Category.builder().nameVn("Ao").build();
        JpaCategory entity = JpaCategory.builder().nameVn("Ao").build();
        JpaCategory savedEntity = JpaCategory.builder().id(1L).nameVn("Ao").build();
        Category saved = Category.builder().id(1L).nameVn("Ao").build();

        when(categoryMapper.toEntity(category)).thenReturn(entity);
        when(springDataCategoryRepository.save(entity)).thenReturn(savedEntity);
        when(categoryMapper.toDomain(savedEntity)).thenReturn(saved);

        Category result = repository.save(category);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_shouldMapWhenFound() {
        JpaCategory entity = JpaCategory.builder().id(2L).nameVn("Quan").build();
        Category domain = Category.builder().id(2L).nameVn("Quan").build();

        when(springDataCategoryRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(categoryMapper.toDomain(entity)).thenReturn(domain);

        Optional<Category> result = repository.findById(2L);

        assertThat(result).isPresent();
        assertThat(result.get().getNameVn()).isEqualTo("Quan");
    }

    @Test
    void findAll_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);
        JpaCategory entity = JpaCategory.builder().id(3L).nameVn("Giay").build();
        Category domain = Category.builder().id(3L).nameVn("Giay").build();

        when(springDataCategoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity), pageable, 1));
        when(categoryMapper.toDomain(entity)).thenReturn(domain);

        Page<Category> result = repository.findAll(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(3L);
    }

    @Test
    void deleteById_shouldDelegate() {
        repository.deleteById(9L);
        verify(springDataCategoryRepository).deleteById(9L);
    }

    @Test
    void hasProducts_shouldReturnFalseByStub() {
        assertThat(repository.hasProducts(10L)).isFalse();
    }
}