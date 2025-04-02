package com.trihydro.library.model;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActiveTimHoldingDeleteModel {

    private List<Long> ids = null;

    public ActiveTimHoldingDeleteModel(List<Long> ids) {
        this.ids = ids;
    }
}
