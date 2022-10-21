package com.docto.protechdoctolib.creneaux;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.GregorianCalendar;

/**
 * Definit une plage de temps dans un journée dans un créneau
 */
@Entity
public class HeuresDebutFin {

    public HeuresDebutFin() {
    }

    public HeuresDebutFin(Long id, Long idCreneaux, LocalTime tempsDebut, LocalTime tempsFin) {
        this.idPlage = id;
        this.idCreneaux = idCreneaux;
        this.tempsDebut = tempsDebut;
        this.tempsFin = tempsFin;
    }

    public  HeuresDebutFin(HeuresDebutFinDTO heuresDebutFinDTO){
        this.idPlage=heuresDebutFinDTO.getIdPlage();
        this.tempsDebut=heuresDebutFinDTO.getTempsDebut();
        this.tempsFin=heuresDebutFinDTO.getTempsFin();
        this.idCreneaux= heuresDebutFinDTO.getIdCreneaux();
    }

    @Id
    @GeneratedValue
    private Long idPlage;


    private  Long idCreneaux;

    @Column
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss[.SSS][.SS][.S]")
    private LocalTime tempsDebut;

    @Column
    @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss[.SSS][.SS][.S]")
    private LocalTime tempsFin;

    public Long getIdPlage() {
        return idPlage;
    }

    public void setIdPlage(Long idPlage) {
        this.idPlage = idPlage;
    }

    public Long getIdCreneaux() {
        return idCreneaux;
    }

    public void setIdCreneaux(Long idCreneaux) {
        this.idCreneaux = idCreneaux;
    }

    public LocalTime getTempsDebut() {
        return tempsDebut;
    }

    public void setTempsDebut(LocalTime tempsDebut) {
        this.tempsDebut = tempsDebut;
    }

    public LocalTime getTempsFin() {
        return tempsFin;
    }

    public void setTempsFin(LocalTime tempsFin) {
        this.tempsFin = tempsFin;
    }
}
