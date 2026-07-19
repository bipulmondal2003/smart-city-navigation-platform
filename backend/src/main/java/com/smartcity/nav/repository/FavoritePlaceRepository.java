package com.smartcity.nav.repository;

import com.smartcity.nav.entity.FavoritePlace;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FavoritePlaceRepository extends JpaRepository<FavoritePlace, Long> {
    List<FavoritePlace> findByUserId(Long userId);
}
