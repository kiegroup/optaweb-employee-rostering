import { HardMediumSoftScore } from 'domain/HardMediumSoftScore';


export interface IndictmentSummary {
  constraintToCountMap: Record<string, number>;
  constraintToScoreImpactMap: Record<string, HardMediumSoftScore>;
}
