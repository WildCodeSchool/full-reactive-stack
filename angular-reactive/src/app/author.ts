export enum Region {
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

export class Author {
  id: number;
  fullName: string;
  region: Region;

  constructor(id: number, fullName: string, region: Region) {
    this.id = id;
    this.fullName = fullName;
    this.region = region;
  }
}
