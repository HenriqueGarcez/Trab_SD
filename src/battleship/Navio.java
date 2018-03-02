/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package battleship;

/**
 *
 * @author elder
 *
 * xi == xfinal --> vertical else horizontal
 */
public class Navio {

    Integer tamanho;
    Integer xInicial, xFinal, yInicial, yFinal;
    char posicoes[];
    
    public Navio(Integer tamanho, Integer xInicial, Integer xFinal, Integer yInicial, Integer yFinal) {
        this.tamanho = tamanho;
        this.xInicial = xInicial;
        this.xFinal = xFinal;
        this.yInicial = yInicial;
        this.yFinal = yFinal;
        posicoes = new char[tamanho];
        //teste
        for(int i=0; i<tamanho; i++)
        {
            //String s = i+ "";
            //posicoes[i] = s.charAt(0);
            posicoes[i] = ' ';
        }
    }

    public Boolean estaAfundado()
    {
        for (int i = 0; i < tamanho; i++) {
            if (posicoes[i] != '_') {
                return false;
            }
        }
        return true;
    }
    private boolean afundou() {

        for (int i = 0; i < tamanho; i++) {
            if (posicoes[i] != 'X') {
                return false;
            }
        }

        return true;
    }

    private boolean descobriu() {
        for (int i = 0; i < tamanho; i++) {
            if (posicoes[i] != ' ') {
                return false;
            }
        }
        return true;
    }

    public TiroEnum atira(Integer x, Integer y) {
        if ((x >= xInicial && x <= xFinal) && (y >= yInicial && y <= yFinal)) {
            int posicao;
            if (xInicial.intValue() == xFinal.intValue() )  {
                //navio na horizontal, y varia
                posicao = (Math.abs(y - yFinal)) ;
            } else {
                posicao = Math.abs(x- xFinal);
            }
            System.out.println("Atirando navio: "+ toString()+ " na coordenada: " + x+y+ " posicao ="+posicao);

            if (posicoes[posicao] == 'X' || posicoes[posicao] == '_') {
                return TiroEnum.AGUA;
            } else {
                if (descobriu()) {
                    posicoes[posicao] = 'X';
                    if (afundou()) {
                        atualizaAfundado();
                        return TiroEnum.AFUNDAR;
                    } else {
                        return TiroEnum.DESCOBERTA;
                    }
                }else {
                    posicoes[posicao] = 'X';
                    if (afundou()) {
                        atualizaAfundado();
                        return TiroEnum.AFUNDAR;
                    } else {
                        return TiroEnum.FOGO;
                    }
                }
            }

        } else {
            return TiroEnum.AGUA;
        }
    }

    public void setxInicial(Integer xInicial) {
        this.xInicial = xInicial;
    }

    public void setxFinal(Integer xFinal) {
        this.xFinal = xFinal;
    }

    public void setyInicial(Integer yInicial) {
        this.yInicial = yInicial;
    }

    public void setyFinal(Integer yFinal) {
        this.yFinal = yFinal;
    }
    
    private void atualizaAfundado()
    {
        for(int i = 0; i< tamanho; i++)
        {
            posicoes[i] = '_';
        }
        
    }
    
    @Override
    public String toString()
    {
        String s=  "Tam =" + tamanho+ " xI= "+ xInicial+ " xF= "+xFinal+ " yI= "+ yInicial+ " yF= "+ yFinal+
                "\nposicoes+";
        for(int i=0; i<tamanho; i++)
        {
            s+= "["+i+"]="+posicoes[i]+ " ";
        }
        return s;
    }

}
