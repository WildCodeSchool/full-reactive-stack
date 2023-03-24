package com.thepracticaldeveloper.reactiveweb.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table
public class Author {

    public static enum Region {
        AuvergneRhôneAlpes,
        BourgogneFrancheComté,
        Bretagne,
        CentreValDeLoire,
        Corse,
        GrandEst,
        HautsdeFrance,
        ÎledeFrance,
        Normandie,
        NouvelleAquitaine,
        Occitanie,
        PaysDeLaLoire,
        ProvenceAlpesCôteDAzur
    };

    @Id
    private Long id;

    private String fullName;
    private Region region;

    // Empty constructor is required by the data layer and JSON de/ser
    public Author() {
    }

    public Author(String fullName, Region region) {
        this.fullName = fullName;
        this.region = region;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Author other = (Author) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Author [id=" + id + ", fullName=" + fullName + ", region=" + region + "]";
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Region getRegion() {
        return region;
    }

}
