
import { BackgroundImage, BackgroundImageSrcMap } from '@patternfly/react-core';
/* eslint-disable camelcase */
import pfbg_1200 from '@patternfly/react-core/dist/styles/assets/images/pfbg_1200.jpg';
import pfbg_576 from '@patternfly/react-core/dist/styles/assets/images/pfbg_576.jpg';
import pfbg_576_2x from '@patternfly/react-core/dist/styles/assets/images/pfbg_576@2x.jpg';
import pfbg_768 from '@patternfly/react-core/dist/styles/assets/images/pfbg_768.jpg';
import pfbg_768_2x from '@patternfly/react-core/dist/styles/assets/images/pfbg_768@2x.jpg';
import * as React from 'react';
/* eslint-enable camelcase */

const bgImages: BackgroundImageSrcMap = {
  lg: pfbg_1200,
  sm: pfbg_768,
  sm2x: pfbg_768_2x,
  xs: pfbg_576,
  xs2x: pfbg_576_2x,
};

const Background: React.FC = () => (
  <BackgroundImage src={bgImages} />
);

export default Background;
