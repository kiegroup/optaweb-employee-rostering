
import { Brand, PageHeader } from '@patternfly/react-core';
import * as React from 'react';
import { useMediaQuery } from 'react-responsive';
import Navigation from './Navigation';
import Toolbar from './Toolbar';

const Header: React.FC<{ onNavToggle: () => void} > = ({ onNavToggle }) => {
  const laptopOrBigger = useMediaQuery({ minWidth: 1400 });
  return (
    <PageHeader
      logo={(
        <Brand src="/assets/images/optaPlannerLogoDarkBackground200px.png" alt="OptaPlanner Logo" />
      )}
      logoProps={{
        href: 'https://www.optaplanner.org',
        target: '_blank',
        rel: 'noopener noreferrer',
      }}
      headerTools={<Toolbar />}
      topNav={laptopOrBigger && <Navigation variant="horizontal" />}
      showNavToggle={!laptopOrBigger}
      onNavToggle={onNavToggle}
      style={{
        backgroundColor: '#0000', // transparent background for background image
      }}
    />
  );
};

export default Header;
