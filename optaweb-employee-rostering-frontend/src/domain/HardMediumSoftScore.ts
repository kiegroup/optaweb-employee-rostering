export function isScoreZero(score: HardMediumSoftScore): boolean {
  return score.hardScore === 0 && score.mediumScore === 0 && score.softScore === 0;
}

export function getHardMediumSoftScoreFromString(hardMediumSoftScore: string): HardMediumSoftScore {
  const components = hardMediumSoftScore.split('/');
  return {
    hardScore: parseInt(components[0], 10),
    mediumScore: parseInt(components[1], 10),
    softScore: parseInt(components[2], 10),
  };
}

export function convertHardMediumSoftScoreToString(hardMediumSoftScore: HardMediumSoftScore) {
  let out = '';
  if (hardMediumSoftScore.hardScore !== 0) {
    out += `${hardMediumSoftScore.hardScore} Hard`;
  }
  if (hardMediumSoftScore.mediumScore !== 0) {
    out = `${out + (out ? '/' : '')}${hardMediumSoftScore.mediumScore} Medium`;
  }
  if (hardMediumSoftScore.softScore !== 0) {
    out = `${out + (out ? '/' : '')}${hardMediumSoftScore.softScore} Soft`;
  }
  return out || '0';
}

export interface HardMediumSoftScore {
  hardScore: number;
  mediumScore: number;
  softScore: number;
}
