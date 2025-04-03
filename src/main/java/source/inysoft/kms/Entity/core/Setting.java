package source.inysoft.kms.Entity.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor

public class Setting {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected long id;


    @Column(nullable = true,columnDefinition = "json")
    protected String menu;

    @Column(nullable = true,columnDefinition = "json")
    protected String sns;

    @Column(nullable = true,columnDefinition = "json")
    protected String agree;

    @Column(nullable = true,columnDefinition = "json")
    protected String email;

}
