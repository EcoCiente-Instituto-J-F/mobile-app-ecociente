package com.example.ecociente.midias;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;

public class EnderecoMidiaTest {

    @Test
    public void preservaExtensaoQuandoElaFazParteDoPublicId() {
        Midia midia = new Midia(
                "icone_c29_muda_solo_raizes",
                Midia.ICONE_CONTEUDO,
                "sustentabilidade",
                "cloudinary",
                "c29_muda-solo-raizes.png",
                "png",
                "c29_muda_solo_raizes",
                "Muda, solo e raízes",
                "ncwfxyfi",
                1793426919L,
                false,
                false,
                false,
                Collections.emptyList());

        String url = EnderecoMidia.cloudinary(midia, false);

        assertEquals(
                "https://res.cloudinary.com/ncwfxyfi/image/upload/"
                        + "c_fit,w_640,h_640,q_auto,f_png/"
                        + "v1793426919/c29_muda-solo-raizes.png.png",
                url);
    }
}
